/**
 */
package person.impl;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.internal.cdo.CDOObjectImpl;
import person.Address;
import person.Person;
import person.PersonPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link person.impl.PersonImpl#getId <em>Id</em>}</li>
 *   <li>{@link person.impl.PersonImpl#getFirstName <em>First Name</em>}</li>
 *   <li>{@link person.impl.PersonImpl#getLastName <em>Last Name</em>}</li>
 *   <li>{@link person.impl.PersonImpl#getYob <em>Yob</em>}</li>
 *   <li>{@link person.impl.PersonImpl#getAddresses <em>Addresses</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PersonImpl extends CDOObjectImpl implements Person {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PersonImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PersonPackage.Literals.PERSON;
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
	public String getId() {
		return (String)eGet(PersonPackage.Literals.PERSON__ID, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		eSet(PersonPackage.Literals.PERSON__ID, newId);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFirstName() {
		return (String)eGet(PersonPackage.Literals.PERSON__FIRST_NAME, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFirstName(String newFirstName) {
		eSet(PersonPackage.Literals.PERSON__FIRST_NAME, newFirstName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLastName() {
		return (String)eGet(PersonPackage.Literals.PERSON__LAST_NAME, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLastName(String newLastName) {
		eSet(PersonPackage.Literals.PERSON__LAST_NAME, newLastName);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getYob() {
		return (Integer)eGet(PersonPackage.Literals.PERSON__YOB, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYob(int newYob) {
		eSet(PersonPackage.Literals.PERSON__YOB, newYob);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public EList<Address> getAddresses() {
		return (EList<Address>)eGet(PersonPackage.Literals.PERSON__ADDRESSES, true);
	}

} //PersonImpl
