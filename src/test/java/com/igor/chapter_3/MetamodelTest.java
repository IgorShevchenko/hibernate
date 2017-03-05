package com.igor.chapter_3;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.igor.setup.DbTestClient;

public class MetamodelTest {

	private static final String PERSISTENCE_UNIT_JPA_XML = "Chapter3_JPA_XML";

	@Test
	public void shouldReadJpaMetamodel() {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML);

		Metamodel metamodel = client.getMetamodel();

		// Describes only Item
		Set<ManagedType<?>> managedTypes = metamodel.getManagedTypes();
		Assertions.assertThat(managedTypes).hasSize(1);

		// Information about entities and embedded classes
		ManagedType<?> managedType = managedTypes.iterator().next();
		Assertions.assertThat(managedType.getJavaType()).isEqualTo(Item.class);
		Assertions.assertThat(managedType.getPersistenceType()).isEqualTo(Type.PersistenceType.ENTITY);

		// Item name
		SingularAttribute<?, ?> nameAttribute = managedType.getSingularAttribute("name");
		Assertions.assertThat(nameAttribute.getJavaType()).isEqualTo(String.class);
		Assertions.assertThat(nameAttribute.getPersistentAttributeType())
				.isEqualTo(Attribute.PersistentAttributeType.BASIC);
		// Not null, e.g. not nullable
		Assertions.assertThat(nameAttribute.isOptional()).isFalse();

		// Item auctionEnd
		SingularAttribute<?, ?> auctionEndAttribute = managedType.getSingularAttribute("auctionEnd");
		Assertions.assertThat(auctionEndAttribute.getJavaType()).isEqualTo(Date.class);
		Assertions.assertThat(auctionEndAttribute.getPersistentAttributeType())
				.isEqualTo(Attribute.PersistentAttributeType.BASIC);
		Assertions.assertThat(auctionEndAttribute.isCollection()).isFalse();
		Assertions.assertThat(auctionEndAttribute.isAssociation()).isFalse();
		Assertions.assertThat(auctionEndAttribute.isOptional()).isTrue();

		client.close();
	}

	@Test
	public void shouldReadJpaStaticMetamodel() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML);

		Item item1 = new Item();
		item1.setName("Item one");

		Item item2 = new Item();
		item2.setName("Item two");

		// Persist two items in database
		client.persist(item1, item2);

		// ONE WAY, not type-safe
		client.executeTransaction((EntityManager em) -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Item> query = cb.createQuery(Item.class);

			Root<Item> fromItem = query.from(Item.class);
			Path<String> namePath = fromItem.get("name");

			String nameParameter = "namePattern";
			query.where(cb.like(namePath, cb.parameter(String.class, nameParameter)));
			query.select(fromItem);

			List<Item> itemsDb = em.createQuery(query)
					.setParameter(nameParameter, "%item one%")
					.getResultList();

			Assertions.assertThat(itemsDb).hasSize(1);
			Assertions.assertThat(itemsDb.get(0).getName()).isEqualTo(item1.getName());
		});

		// SECOND WAY, type-safe, STATIC metamodel, requires annotation processor
		client.executeTransaction((EntityManager em) -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Item> query = cb.createQuery(Item.class);

			Root<Item> fromItem = query.from(Item.class);
			Path<String> namePath = fromItem.get(Item_.name);

			// Hard-coded parameter
			query.where(cb.like(namePath, "%item one%"));
			query.select(fromItem);

			List<Item> itemsDb = em.createQuery(query).getResultList();

			Assertions.assertThat(itemsDb).hasSize(1);
			Assertions.assertThat(itemsDb.get(0).getName()).isEqualTo(item1.getName());
		});

		client.close();
	}

}
