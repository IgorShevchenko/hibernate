package com.igor.chapter_3;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type.PersistenceType;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.Test;

import com.igor.setup.DbTestClient;

/**
 * Override "category" annotation mappings with JPA XML mappings.
 */
public class CategoryTest {

	/**
	 * Declares only "category" JPA XML mapping file, overrides annotations. No
	 * other classes are listed.
	 */
	private static final String PERSISTENCE_UNIT_JPA_XML_OVERRIDE = "Chapter3_JPA_XML_override";

	@Test
	public void shouldOverrideMetadata() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML_OVERRIDE);

		// Category, annotated length = 20
		Category category = new Category();
		category.setName("Very long category name with length overriden in JPA XML mapping");

		// Save category to the database
		client.persist(category);

		// Load category from the database
		Category categoryDb = client.select(Category.class, category.getId());

		// Category name should be reserved
		Assertions.assertThat(categoryDb.getName()).isEqualTo(category.getName());
		Assertions.assertThat(categoryDb.getId()).isNotNull();

		client.close();
	}

	@Test
	public void shouldOverrideAndReadMetadata() throws Exception {

		// JPA doesn't support access to the SQL details
		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML_OVERRIDE);
		Metamodel metamodel = client.getMetamodel();

		// Only category is defined
		Assertions.assertThat(metamodel.getManagedTypes()).hasSize(1);

		ManagedType<Category> categoryType = metamodel.managedType(Category.class);
		Assertions.assertThat(categoryType.getPersistenceType()).isEqualTo(PersistenceType.ENTITY);

		SingularAttribute<? super Category, ?> nameAttribute;
		nameAttribute = categoryType.getSingularAttribute(Category_.name.getName());

		Assertions.assertThat(nameAttribute.getJavaType()).isEqualTo(String.class);
		Assertions.assertThat(nameAttribute.getPersistentAttributeType()).isEqualTo(PersistentAttributeType.BASIC);

		// Now check database metadata
		SessionImplementor delegate = client.getDelegateEm();

		DatabaseMetaData databaseMetaData = delegate.connection().getMetaData();
		ResultSet columnsRs = databaseMetaData.getColumns(null, null, "category", "CATEGORY_NAME");
		columnsRs.next();

		System.out.println(columnsRs.getString("COLUMN_NAME"));
		System.out.println(columnsRs.getString("DATA_TYPE"));
		System.out.println(columnsRs.getString("TYPE_NAME"));
		System.out.println(columnsRs.getString("COLUMN_SIZE"));
		System.out.println(columnsRs.getString("NULLABLE"));
		System.out.println(columnsRs.getString("COLUMN_DEF"));
		System.out.println(columnsRs.getString("IS_NULLABLE"));
		System.out.println(columnsRs.getString("SOURCE_DATA_TYPE"));

		Assertions.assertThat(columnsRs.getString("COLUMN_SIZE")).isEqualTo("255");

		client.close();
	}

	@Test
	public void shouldNotSeePackageInfoJava() throws Exception {

		DbTestClient client = new DbTestClient(PERSISTENCE_UNIT_JPA_XML_OVERRIDE);

		ThrowingCallable throwing = () -> {
			client.executeNamedQuery("findItemsOrderByName_package", Item.class);
		};

		Assertions.assertThatThrownBy(throwing).isInstanceOf(IllegalArgumentException.class);

		client.close();
	}
}
