/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.snomed.api.rest;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.b2international.commons.platform.PlatformUtil;
import com.b2international.snowowl.core.Metadata;
import com.b2international.snowowl.core.MetadataHolder;
import com.b2international.snowowl.core.MetadataHolderMixin;
import com.b2international.snowowl.core.MetadataMixin;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.config.SnowOwlConfiguration;
import com.b2international.snowowl.core.domain.CollectionResource;
import com.b2international.snowowl.datastore.review.BranchState;
import com.b2international.snowowl.datastore.review.ConceptChanges;
import com.b2international.snowowl.datastore.review.ConceptChangesMixin;
import com.b2international.snowowl.datastore.review.Review;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponent;
import com.b2international.snowowl.snomed.api.rest.domain.BranchMixin;
import com.b2international.snowowl.snomed.api.rest.domain.BranchStateMixin;
import com.b2international.snowowl.snomed.api.rest.domain.CollectionResourceMixin;
import com.b2international.snowowl.snomed.api.rest.domain.ISnomedComponentMixin;
import com.b2international.snowowl.snomed.api.rest.domain.ReviewMixin;
import com.b2international.snowowl.snomed.api.rest.util.CsvMessageConverter;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiListingReference;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * The Spring configuration class for Snow Owl's internal REST services module.
 *
 * @since 1.0
 */
@Configuration
@EnableSwagger2
@EnableWebMvc
public class ServicesConfiguration extends WebMvcConfigurerAdapter {

	private ServletContext servletContext;

	private String apiVersion;

	private String apiTitle;
	private String apiTermsOfServiceUrl;
	private String apiContact;
	private String apiLicense;
	private String apiLicenseUrl;
	
	@Autowired
	public void setServletContext(final ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Autowired
	@Value("${api.version}")
	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	@Autowired
	@Value("${api.title}")
	public void setApiTitle(final String apiTitle) {
		this.apiTitle = apiTitle;
	}

	@Autowired
	@Value("${api.termsOfServiceUrl}")
	public void setApiTermsOfServiceUrl(final String apiTermsOfServiceUrl) {
		this.apiTermsOfServiceUrl = apiTermsOfServiceUrl;
	}

	@Autowired
	@Value("${api.contact}")
	public void setApiContact(final String apiContact) {
		this.apiContact = apiContact;
	}

	@Autowired
	@Value("${api.license}")
	public void setApiLicense(final String apiLicense) {
		this.apiLicense = apiLicense;
	}

	@Autowired
	@Value("${api.licenseUrl}")
	public void setApiLicenseUrl(final String apiLicenseUrl) {
		this.apiLicenseUrl = apiLicenseUrl;
	}

	@Bean
	public Docket swaggerSpringMvcPlugin() {
		final TypeResolver resolver = new TypeResolver();
		return new Docket(DocumentationType.SWAGGER_2)
				// sort the api endpoints by their description
				.apiListingReferenceOrdering(Ordering.from(new Comparator<ApiListingReference>() {
					@Override
					public int compare(ApiListingReference o1, ApiListingReference o2) {
						return o1.getDescription().compareTo(o2.getDescription());
					}
				}))
	            .apiInfo(apiInfo())
	            .pathProvider(new RelativePathProvider(servletContext))
	            .useDefaultResponseMessages(false)
	            .ignoredParameterTypes(Principal.class, Void.class)
	            .genericModelSubstitutes(ResponseEntity.class)
	            .genericModelSubstitutes(DeferredResult.class)
	            .directModelSubstitute(Branch.class, BranchMixin.class)
	            .directModelSubstitute(Map.class, Object.class)
	            .directModelSubstitute(Metadata.class, Object.class)
	            .directModelSubstitute(UUID.class, String.class)
	            .alternateTypeRules(
	            	new AlternateTypeRule(resolver.resolve(DeferredResult.class, resolver.resolve(ResponseEntity.class, WildcardType.class)), resolver.resolve(WildcardType.class))
	            );
	}
	
	private ApiInfo apiInfo() {
		final Contact contact = new Contact("B2i Healthcare", "http://b2i.sg", apiContact);
		return new ApiInfo(apiTitle, readApiDescription(), apiVersion, apiTermsOfServiceUrl, contact, apiLicense, apiLicenseUrl);
	}

	private String readApiDescription() {
		try {
			final File apiDesc = new File(PlatformUtil.toAbsolutePath(ServicesConfiguration.class, "api-description.html"));
			return Joiner.on("\n").join(Files.readLines(apiDesc, Charsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Failed to read api-description.html file", e);
		}
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new GuavaModule());
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
		final ISO8601DateFormat df = new ISO8601DateFormat();
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		objectMapper.setDateFormat(df);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.addMixInAnnotations(CollectionResource.class, CollectionResourceMixin.class);
		objectMapper.addMixInAnnotations(SnomedComponent.class, ISnomedComponentMixin.class);
		objectMapper.addMixInAnnotations(ISnomedBrowserComponent.class, ISnomedComponentMixin.class);
		objectMapper.addMixInAnnotations(Branch.class, BranchMixin.class);
		objectMapper.addMixInAnnotations(Metadata.class, MetadataMixin.class);
		objectMapper.addMixInAnnotations(MetadataHolder.class, MetadataHolderMixin.class);
		objectMapper.addMixInAnnotations(Review.class, ReviewMixin.class);
		objectMapper.addMixInAnnotations(BranchState.class, BranchStateMixin.class);
		objectMapper.addMixInAnnotations(ConceptChanges.class, ConceptChangesMixin.class);
		return objectMapper;
	}
	
	@Bean
	public IEventBus eventBus() {
		return com.b2international.snowowl.core.ApplicationContext.getInstance().getServiceChecked(IEventBus.class);
	}
	
	@Bean
	public Integer maxReasonerRuns() {
		return com.b2international.snowowl.core.ApplicationContext.getInstance()
				.getServiceChecked(SnowOwlConfiguration.class)
				.getModuleConfig(SnomedCoreConfiguration.class)
				.getMaxReasonerRuns();
	}
	
	@Override
	public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
		final StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
		stringConverter.setWriteAcceptCharset(false);
		converters.add(stringConverter);

		converters.add(new ByteArrayHttpMessageConverter());
		converters.add(new ResourceHttpMessageConverter());
		converters.add(new CsvMessageConverter());

		final MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
		jacksonConverter.setObjectMapper(objectMapper());
		converters.add(jacksonConverter);
	}

	@Override
	public void configurePathMatch(final PathMatchConfigurer configurer) {
		configurer.setUseRegisteredSuffixPatternMatch(true);
		configurer.setPathMatcher(new AntPathWildcardMatcher());
	}
}
