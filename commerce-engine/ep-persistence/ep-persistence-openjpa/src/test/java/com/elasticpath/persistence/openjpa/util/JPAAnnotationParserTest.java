/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.persistence.openjpa.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Lists;

import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.persistence.ElementType;
import org.assertj.core.api.Condition;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit test for the {@code JPAAnnotationParser} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class JPAAnnotationParserTest {

	@InjectMocks private JPAAnnotationParser jpaAnnotationParser;

	@Mock private ClassMetaData classMetaDataWithOneToOneAnnotation;
	@Mock private ClassMetaData classMetaDataWithOneToManyAnnotation;
	@Mock private ClassMetaData classMetaDataWithManyToOneAnnotation;
	@Mock private ClassMetaData classMetaDataWithManyToManyAnnotation;
	@Mock private ClassMetaData classMetaDataWithDependentEntityAsTarget;
	@Mock private ClassMetaData classMetaDataWithDependentEntityAsElementType;
	@Mock private ClassMetaData classMetaDataWithInheritedOneToOneAnnotation;

	private final Condition<Set<String>> containsOnlyTargetEntityCondition = new Condition<Set<String>>("Check if only target entity exists") {
		@Override
		public boolean matches(final Set<String> value) {
			return value.contains(DependentEntityAsTargetImpl.class.getSimpleName());
		}
	};

	private final Condition<Set<String>> containsBothTargetAndElementTypeEntitiesyCondition =
		new Condition<Set<String>>("Check if both target and element type entities exist") {

		List<String> entitiesToCheck = Lists.newArrayList(DependentEntityAsTargetImpl.class.getSimpleName(),
			DependentEntityAsElementTypeImpl.class.getSimpleName());

		@Override
		public boolean matches(final Set<String> value) {
			return value.containsAll(entitiesToCheck);
		}
	};

	private final Condition<Set<String>> containsAssociatedAndTargetEntitiesCondition =
		new Condition<Set<String>>("Check if both associated and target type entities exist") {

			List<String> entitiesToCheck = Lists.newArrayList(DependentEntityAsTargetImpl.class.getSimpleName(),
				AssociatedEntityImpl.class.getSimpleName());

			@Override
			public boolean matches(final Set<String> value) {
				return value.containsAll(entitiesToCheck);
			}
		};

	/**
	 * Test parsing entities with OneToOne annotation.
	 */
	@Test
	public final void shouldParseEntityClassWithOneToOneAnnotation() {

		doReturn(CustomEntityWithOneToOneAnnotationImpl.class).when(classMetaDataWithOneToOneAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithOneToOneAnnotation});

		assertThat(jpaAnnotationParser.getClassNameToSetOfJPAFields())
			.hasEntrySatisfying(CustomEntityWithOneToOneAnnotationImpl.class.getSimpleName(), containsOnlyTargetEntityCondition);
	}

	/**
	 * Test parsing entities with OneToOne annotation.
	 */
	@Test
	public final void shouldParseInheritedEntityClassWithOneToOneAnnotation() {

		doReturn(InheritedCustomEntityWithOneToOneAnnotationImpl.class).when(classMetaDataWithInheritedOneToOneAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithInheritedOneToOneAnnotation});

		assertThat(jpaAnnotationParser.getClassNameToSetOfJPAFields())
			.hasEntrySatisfying(InheritedCustomEntityWithOneToOneAnnotationImpl.class.getSimpleName(), containsAssociatedAndTargetEntitiesCondition);
	}

	/**
	 * Test parsing entities with OneToMany annotation.
	 */
	@Test
	public final void shouldParseEntityClassWithOneToManyAnnotation() {

		doReturn(CustomEntityWithOneToManyAnnotationImpl.class).when(classMetaDataWithOneToManyAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithOneToManyAnnotation});

		assertThat(jpaAnnotationParser.getClassNameToSetOfJPAFields())
			.hasEntrySatisfying(CustomEntityWithOneToManyAnnotationImpl.class.getSimpleName(), containsBothTargetAndElementTypeEntitiesyCondition);
	}

	/**
	 * Test parsing entities with ManyToOne annotation.
	 */
	@Test
	public final void shouldParseEntityClassWithManyToOneAnnotation() {

		doReturn(CustomEntityWithManyToOneAnnotationImpl.class).when(classMetaDataWithManyToOneAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithManyToOneAnnotation});

		assertThat(jpaAnnotationParser.getClassNameToSetOfJPAFields())
			.hasEntrySatisfying(CustomEntityWithManyToOneAnnotationImpl.class.getSimpleName(), containsOnlyTargetEntityCondition);
	}

	/**
	 * Test parsing entities with ManyToMany annotation.
	 */
	@Test
	public final void shouldParseEntityClassWithManyToManyAnnotation() {

		doReturn(CustomEntityWithManyToManyAnnotationImpl.class).when(classMetaDataWithManyToManyAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithManyToManyAnnotation});

		assertThat(jpaAnnotationParser.getClassNameToSetOfJPAFields())
			.hasEntrySatisfying(CustomEntityWithManyToManyAnnotationImpl.class.getSimpleName(), containsBothTargetAndElementTypeEntitiesyCondition);
	}

	/**
	 * Test coupling between queried and modified entities (queried entity has modified entity as a field).
	 *
	 * E.g. ShoppingCartMementoImpl is modified, but ShoppingItemImpl is queried. Because ShoppingCartMementoImpl has ShoppingItemImpl as one of
	 * its fields, these 2 are coupled.
	 */
	@Test
	public final void shouldReturnTrueWhenModifiedEntityHasQueriedEntityAsAField() {

		doReturn(CustomEntityWithOneToOneAnnotationImpl.class).when(classMetaDataWithOneToOneAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithOneToOneAnnotation});

		assertThat(jpaAnnotationParser
			.isQueriedEntityCoupledToModifiedEntity(DependentEntityAsTargetImpl.class.getSimpleName(),
				CustomEntityWithOneToOneAnnotationImpl.class.getSimpleName()))
			.isTrue();

	}

	/**
	 * Test coupling between queried and modified entities (modified entity  queried  entity as a field).
	 *
	 * E.g. CustomerImpl is modified, but ShopperMementoImpl is queried. CustomerImpl does not have ShopperMementoImpl field but
	 * ShopperMementoImpl has CustomerImpl. Hence, they are coupled.
	 */
	@Test
	public final void shouldReturnTrueIfQueriedEntityHasModifiedEntityAsAField() {

		doReturn(DependentEntityAsElementTypeImpl.class).when(classMetaDataWithDependentEntityAsElementType).getDescribedType();
		doReturn(DependentEntityAsTargetImpl.class).when(classMetaDataWithDependentEntityAsTarget).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithDependentEntityAsElementType, classMetaDataWithDependentEntityAsTarget});

		assertThat(jpaAnnotationParser
			.isQueriedEntityCoupledToModifiedEntity(DependentEntityAsTargetImpl.class.getSimpleName(),
				DependentEntityAsElementTypeImpl.class.getSimpleName()))
			.isTrue();

	}

	/**
	 * Test the case when queried and modified entities are not coupled.
	 */
	@Test
	public final void shouldReturnFalseWhenQueriedAndModifiedEntitiesAreNotCoupled() {

		doReturn(CustomEntityWithOneToOneAnnotationImpl.class).when(classMetaDataWithOneToOneAnnotation).getDescribedType();

		jpaAnnotationParser.parse(new ClassMetaData[]{classMetaDataWithOneToOneAnnotation});

		assertThat(jpaAnnotationParser
			.isQueriedEntityCoupledToModifiedEntity(DependentEntityAsTargetImpl.class.getSimpleName(),
				"DummyEntityImpl"))
			.isFalse();

	}

	private class CustomEntityWithOneToOneAnnotationImpl {
		private DependentEntityAsTargetImpl dependentEntity;

		@OneToOne(targetEntity = DependentEntityAsTargetImpl.class)
		public DependentEntityAsTargetImpl getDependentEntity() {
			return dependentEntity;
		}
	}

	private class InheritedCustomEntityWithOneToOneAnnotationImpl extends CustomEntityWithOneToOneAnnotationImpl {
		private AssociatedEntityImpl associatedEntity;

		@OneToOne(targetEntity = AssociatedEntityImpl.class)
		public AssociatedEntityImpl fetchMeAssociatedEntity() {
			return associatedEntity;
		}
	}

	private class CustomEntityWithOneToManyAnnotationImpl {
		private List<DependentEntityAsTargetImpl> dependentEntities;
		private List<DependentEntityAsElementTypeImpl> dependentEntityAsElementTypeImplList;

		@OneToMany(targetEntity = DependentEntityAsTargetImpl.class)
		public List<DependentEntityAsTargetImpl> getDependentEntities() {
			return dependentEntities;
		}

		@OneToMany
		@ElementType(value = DependentEntityAsElementTypeImpl.class)
		public List<DependentEntityAsElementTypeImpl> getDependentEntityAsElementTypeList() {
			return dependentEntityAsElementTypeImplList;
		}
	}

	private class CustomEntityWithManyToOneAnnotationImpl {
		private DependentEntityAsTargetImpl dependentEntity;

		@ManyToOne(targetEntity = DependentEntityAsTargetImpl.class)
		public DependentEntityAsTargetImpl getDependentEntity() {
			return dependentEntity;
		}
	}

	private class CustomEntityWithManyToManyAnnotationImpl {
		private List<DependentEntityAsTargetImpl> dependentEntities;
		private List<DependentEntityAsElementTypeImpl> dependentEntityAsElementTypeImplList;

		@ManyToMany(targetEntity = DependentEntityAsTargetImpl.class)
		public List<DependentEntityAsTargetImpl> getDependentEntities() {
			return dependentEntities;
		}

		@ManyToMany
		@ElementType(value = DependentEntityAsElementTypeImpl.class)
		public List<DependentEntityAsElementTypeImpl> getDependentEntityAsElementTypeList() {
			return dependentEntityAsElementTypeImplList;
		}
	}

	private class DependentEntityAsTargetImpl {
		private DependentEntityAsElementTypeImpl dependentEntityAsElementType;

		@OneToOne(targetEntity = DependentEntityAsElementTypeImpl.class)
		public DependentEntityAsElementTypeImpl getDependentEntityAsElementType() {
			return dependentEntityAsElementType;
		}
	}

	private class DependentEntityAsElementTypeImpl {
		//empty class
	}

	private class AssociatedEntityImpl {
		//empty class
	}
}
