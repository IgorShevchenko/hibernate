
// See org.hibernate.id.enhanced.TableGenerator. You find parameters in the code or description.
@org.hibernate.annotations.GenericGenerator(name = "IGOR_GENERATOR", strategy = "enhanced-table", parameters = {
		@org.hibernate.annotations.Parameter(name = "prefer_entity_table_as_segment_value", value = "true"),
		@org.hibernate.annotations.Parameter(name = "initial_value", value = "1000") })

package com.igor.chapter_5;