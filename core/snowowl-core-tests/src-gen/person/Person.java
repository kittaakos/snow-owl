/**
 */
package person;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link person.Person#getId <em>Id</em>}</li>
 *   <li>{@link person.Person#getFirstName <em>First Name</em>}</li>
 *   <li>{@link person.Person#getLastName <em>Last Name</em>}</li>
 *   <li>{@link person.Person#getYob <em>Yob</em>}</li>
 *   <li>{@link person.Person#getAddresses <em>Addresses</em>}</li>
 * </ul>
 * </p>
 *
 * @see person.PersonPackage#getPerson()
 * @model
 * @generated
 */
public interface Person extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see person.PersonPackage#getPerson_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link person.Person#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>First Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>First Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Name</em>' attribute.
	 * @see #setFirstName(String)
	 * @see person.PersonPackage#getPerson_FirstName()
	 * @model
	 * @generated
	 */
	String getFirstName();

	/**
	 * Sets the value of the '{@link person.Person#getFirstName <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Name</em>' attribute.
	 * @see #getFirstName()
	 * @generated
	 */
	void setFirstName(String value);

	/**
	 * Returns the value of the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Last Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Name</em>' attribute.
	 * @see #setLastName(String)
	 * @see person.PersonPackage#getPerson_LastName()
	 * @model
	 * @generated
	 */
	String getLastName();

	/**
	 * Sets the value of the '{@link person.Person#getLastName <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Name</em>' attribute.
	 * @see #getLastName()
	 * @generated
	 */
	void setLastName(String value);

	/**
	 * Returns the value of the '<em><b>Yob</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Yob</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Yob</em>' attribute.
	 * @see #setYob(int)
	 * @see person.PersonPackage#getPerson_Yob()
	 * @model
	 * @generated
	 */
	int getYob();

	/**
	 * Sets the value of the '{@link person.Person#getYob <em>Yob</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Yob</em>' attribute.
	 * @see #getYob()
	 * @generated
	 */
	void setYob(int value);

	/**
	 * Returns the value of the '<em><b>Addresses</b></em>' containment reference list.
	 * The list contents are of type {@link person.Address}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Addresses</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Addresses</em>' containment reference list.
	 * @see person.PersonPackage#getPerson_Addresses()
	 * @model containment="true"
	 * @generated
	 */
	EList<Address> getAddresses();

} // Person
