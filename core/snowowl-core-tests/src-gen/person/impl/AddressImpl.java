/**
 */
package person.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.internal.cdo.CDOObjectImpl;
import person.Address;
import person.PersonPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Address</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link person.impl.AddressImpl#getCountry <em>Country</em>}</li>
 *   <li>{@link person.impl.AddressImpl#getCity <em>City</em>}</li>
 *   <li>{@link person.impl.AddressImpl#getStreet <em>Street</em>}</li>
 *   <li>{@link person.impl.AddressImpl#getZipCode <em>Zip Code</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class AddressImpl extends CDOObjectImpl implements Address {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AddressImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PersonPackage.Literals.ADDRESS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected int eStaticFeatureCount() {
		return 0;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCountry() {
		return (String)eGet(PersonPackage.Literals.ADDRESS__COUNTRY, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCountry(String newCountry) {
		eSet(PersonPackage.Literals.ADDRESS__COUNTRY, newCountry);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getCity() {
		return (String)eGet(PersonPackage.Literals.ADDRESS__CITY, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCity(String newCity) {
		eSet(PersonPackage.Literals.ADDRESS__CITY, newCity);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getStreet() {
		return (String)eGet(PersonPackage.Literals.ADDRESS__STREET, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setStreet(String newStreet) {
		eSet(PersonPackage.Literals.ADDRESS__STREET, newStreet);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getZipCode() {
		return (Integer)eGet(PersonPackage.Literals.ADDRESS__ZIP_CODE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setZipCode(int newZipCode) {
		eSet(PersonPackage.Literals.ADDRESS__ZIP_CODE, newZipCode);
	}

} //AddressImpl
