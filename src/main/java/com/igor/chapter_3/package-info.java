// Global metadata, package-level annotations. Hibernate extension of javax.persistence.NamedQuery
// Otherwise need to put annotation on some MyNamedQueries.java class as part of domain model or in XML file.
// Can define them in HBM.XML, see item-metadata.hbm.xml

@org.hibernate.annotations.NamedQueries({
    @org.hibernate.annotations.NamedQuery(
        name = "findItemsOrderByName_package",
        query = "select i from Item i order by i.name asc"
    )
    ,
    @org.hibernate.annotations.NamedQuery(
        name = "findItemBuyNowPriceGreaterThan_package",
        query = "select i from Item i where i.buyNowPrice > :price",
        timeout = 60, // Seconds!
        comment = "Custom SQL comment"
    )
})

package com.igor.chapter_3;
