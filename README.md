Orika ! [![Build Status](https://secure.travis-ci.org/orika-mapper/orika.png)](http://travis-ci.org/orika-mapper/orika)
-----------------------------------------------------------------------

**NEW** We are pleased to announce the release of Orika **1.4.3** ! _This version is available on Maven central repository_ 


Also, check out our new [FAQ](https://github.com/elaatifi/orika/wiki/FAQ)


What?
=====

Orika is a Java Bean mapping framework that recursively copies (among other capabilities) data from one object to another. It can be very useful when developing multi-layered applications.

Why?
=====
Struggling with hand coded and reflection-based mappers? Orika can be used to simplify the process of mapping between one object layer and another.

Our ambition is to build a comprehensive, efficient and robust Java bean mapping solution. Orika focuses on automating as much as possible, while providing customization  through configuration and extension where needed.

Orika enables the developer to :
 * Map complex and deeply structured objects
 * "Flatten" or "Expand" objects by mapping nested properties to top-level properties, and vice versa
 * Create mappers on-the-fly, and apply customizations to control some or all of the mapping
 * Create converters for complete control over the mapping of a specific set of objects anywhere in the object graph--by type, or even by specific property name
 * Handle proxies or enhanced objects (like those of Hibernate, or the various mock frameworks)
 * Apply bi-directional mapping with one configuration
 * Map to instances of an appropriate concrete class for a target abstract class or interface
 
How?
=====

Orika uses byte code generation to create fast mappers with minimal overhead. Take a look at this simple performance test : http://bit.ly/pJ7n6t


Want to give Orika a try? Check out our new [User Guide](http://orika-mapper.github.com/orika-docs/) 

_looking for the old GettingStartedGuide? it's [here](Check out the http://code.google.com/p/orika/wiki/GettingStartedGuide)_
