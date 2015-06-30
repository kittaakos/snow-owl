/**
 */
package person.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

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
public class PersonImpl extends MinimalEObjectImpl.Container implements Person {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getFirstName() <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstName()
	 * @generated
	 * @ordered
	 */
	protected static final String FIRST_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFirstName() <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstName()
	 * @generated
	 * @ordered
	 */
	protected String firstName = FIRST_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastName() <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastName()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastName() <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastName()
	 * @generated
	 * @ordered
	 */
	protected String lastName = LAST_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getYob() <em>Yob</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYob()
	 * @generated
	 * @ordered
	 */
	protected static final int YOB_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getYob() <em>Yob</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getYob()
	 * @generated
	 * @ordered
	 */
	protected int yob = YOB_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAddresses() <em>Addresses</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddresses()
	 * @generated
	 * @ordered
	 */
	protected EList<Address> addresses;

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
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(String newId) {
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PersonPackage.PERSON__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setFirstName(String newFirstName) {
		String oldFirstName = firstName;
		firstName = newFirstName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PersonPackage.PERSON__FIRST_NAME, oldFirstName, firstName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLastName(String newLastName) {
		String oldLastName = lastName;
		lastName = newLastName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PersonPackage.PERSON__LAST_NAME, oldLastName, lastName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getYob() {
		return yob;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setYob(int newYob) {
		int oldYob = yob;
		yob = newYob;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PersonPackage.PERSON__YOB, oldYob, yob));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Address> getAddresses() {
		if (addresses == null) {
			addresses = new EObjectContainmentEList<Address>(Address.class, this, PersonPackage.PERSON__ADDRESSES);
		}
		return addresses;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PersonPackage.PERSON__ADDRESSES:
				return ((InternalEList<?>)getAddresses()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PersonPackage.PERSON__ID:
				return getId();
			case PersonPackage.PERSON__FIRST_NAME:
				return getFirstName();
			case PersonPackage.PERSON__LAST_NAME:
				return getLastName();
			case PersonPackage.PERSON__YOB:
				return getYob();
			case PersonPackage.PERSON__ADDRESSES:
				return getAddresses();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PersonPackage.PERSON__ID:
				setId((String)newValue);
				return;
			case PersonPackage.PERSON__FIRST_NAME:
				setFirstName((String)newValue);
				return;
			case PersonPackage.PERSON__LAST_NAME:
				setLastName((String)newValue);
				return;
			case PersonPackage.PERSON__YOB:
				setYob((Integer)newValue);
				return;
			case PersonPackage.PERSON__ADDRESSES:
				getAddresses().clear();
				getAddresses().addAll((Collection<? extends Address>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PersonPackage.PERSON__ID:
				setId(ID_EDEFAULT);
				return;
			case PersonPackage.PERSON__FIRST_NAME:
				setFirstName(FIRST_NAME_EDEFAULT);
				return;
			case PersonPackage.PERSON__LAST_NAME:
				setLastName(LAST_NAME_EDEFAULT);
				return;
			case PersonPackage.PERSON__YOB:
				setYob(YOB_EDEFAULT);
				return;
			case PersonPackage.PERSON__ADDRESSES:
				getAddresses().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PersonPackage.PERSON__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case PersonPackage.PERSON__FIRST_NAME:
				return FIRST_NAME_EDEFAULT == null ? firstName != null : !FIRST_NAME_EDEFAULT.equals(firstName);
			case PersonPackage.PERSON__LAST_NAME:
				return LAST_NAME_EDEFAULT == null ? lastName != null : !LAST_NAME_EDEFAULT.equals(lastName);
			case PersonPackage.PERSON__YOB:
				return yob != YOB_EDEFAULT;
			case PersonPackage.PERSON__ADDRESSES:
				return addresses != null && !addresses.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (id: ");
		result.append(id);
		result.append(", firstName: ");
		result.append(firstName);
		result.append(", lastName: ");
		result.append(lastName);
		result.append(", yob: ");
		result.append(yob);
		result.append(')');
		return result.toString();
	}

} //PersonImpl
