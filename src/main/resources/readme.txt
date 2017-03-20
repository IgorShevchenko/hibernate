
1) SETUP LOGGING

Run with VM argument:
-Djava.util.logging.config.file=src/main/resources/logging.properties

In Eclipse setup 'Arguments' for 'Run Configurations' and 'Debug Configurations'.
Global setup: Preferences -> Java -> Installed JREs -> Edit... -> Default VM arguments

2) Every interaction with your database should occur within explicit transaction boundaries, even if you are only reading data.

3) SETUP ANNOTATION PROCESSING 
Enable Maven -> Annotation Processing
Automatically configure JDT APT

4) MySQL all queries log
SET GLOBAL general_log = 1;
SHOW VARIABLES LIKE "general_log%";



@Access
@AttributeOverride
@AttributeOverrides
@Basic
@Column
@ColumnDefault
@ColumnTransformer
@CreationTimestamp
@DynamicInsert
@DynamicUpdate
@Embeddable
@Embedded
@EmbeddedId
@Entity
@Enumerated
@Formula
@Future
@Generated
@GeneratedValue
@GenericGenerator
@Id
@Immutable
@JoinColumn
@ManyToOne
@MappedSuperclass 
@NamedQueries
@NamedQuery
@NotNull 
@Parameter
@SelectBeforeUpdate
@Size
@Subselect
@Synchronize
@Table
@Temporal
@Transient
@UpdateTimestamp
@Version

@Lob
@Fetch


