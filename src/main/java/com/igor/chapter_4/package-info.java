
// See org.hibernate.id.enhanced.TableGenerator
@org.hibernate.annotations.GenericGenerator(name = "IGOR_GENERATOR", strategy = "enhanced-table", parameters = {
		@org.hibernate.annotations.Parameter(name = "prefer_entity_table_as_segment_value", value = "true"),
		@org.hibernate.annotations.Parameter(name = "initial_value", value = "1000") })

/*
 * Table "jpwh_sequence", next_val bigint(20). Works with Integer IDs. The
 * enhanced-sequence strategy produces sequential numeric values. Recommended
 * for most applications. If your SQL dialect supports sequences, Hibernate will
 * use an actual database sequence. If your DBMS doesn’t support native
 * sequences, Hibernate will manage and use an extra “sequence table,”
 * simulating the behavior of a sequence. Called before performing an SQL
 * INSERT.
 */
@org.hibernate.annotations.GenericGenerator(name = "ID_GENERATOR", strategy = "enhanced-sequence", parameters = {

		/* Table name, defaults to 'hibernate_sequence' */
		@org.hibernate.annotations.Parameter(name = "sequence_name", value = "JPWH_SEQUENCE"),

		/* Initial/first value to use */
		@org.hibernate.annotations.Parameter(name = "initial_value", value = "1000") })

package com.igor.chapter_4;