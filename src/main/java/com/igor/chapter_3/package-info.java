// Global metadata, package-level annotations. Hibernate extension of javax.persistence.NamedQuery
// Otherwise need to put annotation on some MyNamedQueries.java class as part of domain model or in XML file.
// Can put them into separate mapping XML file?

@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
        name = "findItemsOrderByName",
        query = "select i from Item i order by i.name asc"
    )
    ,
    @org.hibernate.annotations.NamedQuery(
        name = "findItemBuyNowPriceGreaterThan",
        query = "select i from Item i where i.buyNowPrice > :price",
        timeout = 60, // Seconds!
        comment = "Custom SQL comment"
    )
})

package com.igor.chapter_3;
