/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.ElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.base.exception.EpSystemException;


/**
 * This class parses all available (OOTB and extension) entity classes containing JPA (One2X and ManyToX) annotations.
 * The HDS feature relies on the list of modified entities when deciding whether a query needs to be executed on master or replica db.
 *
 * However, this list is not enough due additional cases when e.g. entity A is modified but accessed via B.getA() (Customer and ShopperMemento) or
 * when entity A is modified but A.B field is queried (ShoppingCartMementoImpl and ShoppingItemImpl).
 *
 * The final result of parsing will be stored in a map with entity name as a key and a set of JPA annotated fields.
 *
 * E.g. CustomerImpl => [CustomerAddressImpl, CustomerGroupImpl, CustomerProfileValueImpl, AbstractPaymentMethodImpl]
 */
@SuppressWarnings({"unchecked"})
public class JPAAnnotationParser {

	private static final Logger LOG = LoggerFactory.getLogger(JPAAnnotationParser.class);
	private static final Class<?>[] JPA_ANNOTATIONS = new Class<?>[]{OneToMany.class, OneToOne.class, ManyToMany.class, ManyToOne.class};

	private final Map<String, Set<String>> classNameToSetOfJPAFields = new HashMap<>();

	/**
	 * Parse all available classes with JPA annotation.
	 *
	 * @param classMetaDatas the array of class meta-data instances obtained from read-write entity manager's meta repository.
	 *
	 */
	public void parse(final ClassMetaData[] classMetaDatas) {

		for (ClassMetaData classMetaData : classMetaDatas) {
			Class<?> entityClass = classMetaData.getDescribedType();

			for (Method method : getAllClassMethods(entityClass)) {
				if (shouldNotIgnoreMethod(method.getReturnType().getPackage())) {
					for (Class<?> jpaAnnotation : JPA_ANNOTATIONS) {
						if (processJPAAnnotation(entityClass, method, jpaAnnotation)) {
							break;
						}
					}
				}
			}
		}

		LOG.debug("class name to set of JPA fields map \n {}", classNameToSetOfJPAFields);
	}

	private boolean shouldNotIgnoreMethod(final Package methodPackage) {
		return methodPackage != null && !methodPackage.getName().startsWith("java.lang");
	}

	//combine all methods - local and inherited
	private Set<Method> getAllClassMethods(final Class<?> entityClass) {
		Set<Method> allClassMethods = Sets.newHashSet(entityClass.getDeclaredMethods());
		allClassMethods.addAll(Arrays.asList(entityClass.getMethods()));

		return allClassMethods;
	}

	@SuppressWarnings("rawtypes")
	private boolean processJPAAnnotation(final Class<?> processingEntityClass, final Method method, final Class annotationClass) {
		Annotation annotation = method.getAnnotation(annotationClass);

		if (annotation != null) {

			//targetEntity method exists in all OneToX and ManyToX annotations
			String targetEntityName = getTargetEntityClass(annotation, "targetEntity").getSimpleName();

			if ("void".equals(targetEntityName)) { //but may not be used, thus void is returned
				targetEntityName = processVoidTargetEntityIfRequired(method);
			}

			if (StringUtils.isNotBlank(targetEntityName)) {
				Set<String> jpaFields = classNameToSetOfJPAFields.computeIfAbsent(processingEntityClass.getSimpleName(), val -> new HashSet<>());
				jpaFields.add(targetEntityName);

				return true;
			}
		}

		return false;
	}

	private String processVoidTargetEntityIfRequired(final Method entityDeclaredMethod) {

		Annotation annotation = entityDeclaredMethod.getAnnotation(ElementType.class);

		if (annotation != null) {
			return getTargetEntityClass(annotation, "value")
				.getSimpleName();
		}

		return "";
	}

	@SuppressWarnings("PMD.PreserveStackTrace")
	private Class<?> getTargetEntityClass(final Annotation annotation, final String methodName) {

		Class<?> annotationDeclaringClass = annotation.getClass().getDeclaringClass();

		Method method;

		try {
			method = annotation.getClass().getDeclaredMethod(methodName);
		} catch (NoSuchMethodException nsme) {
			try {
				method =  annotation.getClass().getMethod(methodName);
			} catch (NoSuchMethodException nsme2) {
				String errMessage = String.format("Method [%s] doesn't exist in annotation [%s] found in the class [%s]", methodName, annotation,
					annotationDeclaringClass);

				throw new EpSystemException(errMessage);
			}
		}

		try {
			return (Class) method.invoke(annotation);
		} catch (Exception e) {
			String errMessage = String.format("Error occurred while invoking a method [%s] on annotation [%s] found in the class [%s]",
				method, annotation, annotationDeclaringClass);

			throw new EpSystemException(errMessage, e);
		}
	}

	/**
	 * Check if queried entity has modified entity as a field and vice versa.
	 * If any of the above is true, then query, containing queried entity, must be executed on master.
	 *
	 * @param queriedEntity the queried entity (taken from the JPA query)
	 * @param modifiedEntity modified entity
	 * @return the synonym class name or null.
	 */
	public boolean isQueriedEntityCoupledToModifiedEntity(final String queriedEntity, final String modifiedEntity) {

		//check if modified entity has queried entity as a field
		// e.g. whether CustomerImpl has ShopperMementoImpl or whether ShoppingCartMementoImpl has ShoppingItemImpl
		// in the first case, it will be false; in the second case, true

		//if yes, exit
		if (areEntitiesCoupled(queriedEntity, modifiedEntity)) {
			return true;
		}

		//check the other side of the relation - for CustomerImpl <-> ShopperMementoImpl, it will be true
		return areEntitiesCoupled(modifiedEntity, queriedEntity);
	}

	private boolean areEntitiesCoupled(final String queriedEntityToCheck, final String targetEntity) {
		Set<String> entityJPAFields = classNameToSetOfJPAFields.get(targetEntity);
		return entityJPAFields != null && entityJPAFields.contains(queriedEntityToCheck);
	}

	/**
	 * Visible for testing.
	 * @return map with class name to set of JPA fields entries.
	 */
	@VisibleForTesting
	Map<String, Set<String>> getClassNameToSetOfJPAFields() {
		return classNameToSetOfJPAFields;
	}
}
