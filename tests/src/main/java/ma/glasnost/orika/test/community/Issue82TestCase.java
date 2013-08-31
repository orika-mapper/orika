package ma.glasnost.orika.test.community;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.unenhance.HibernateUnenhanceStrategy;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alexey Salnikov
 */
public class Issue82TestCase {
    private MapperFacade mapperFacade;
    
    @Before
    public void init() {
        DefaultMapperFactory.Builder builder = new DefaultMapperFactory.Builder();
        builder.unenhanceStrategy(new HibernateUnenhanceStrategy());
        builder.classMapBuilderFactory(new MyClassMapBuilderFactory());
        MapperFactory mapperFactory = builder
                .compilerStrategy(new EclipseJdtCompilerStrategy()).build();
        mapperFacade = mapperFactory.getMapperFacade();
    }
    
    @Test
    public void testMapDtoToEntity() {
        DocTypeDto docTypeDto = new DocTypeDto();
        docTypeDto.setId(1L);
        docTypeDto.setCaption("Test document type");
        
        DocStatusDto docStatusDto = new DocStatusDto();
        docStatusDto.setId(1L);
        docStatusDto.setDocTypeId(docTypeDto.getId());
        docStatusDto.setCaption("Draft");
        
        SomeDocumentDto documentDto = new SomeDocumentDto();
        documentDto.setId(1L);
        documentDto.setType(docTypeDto);
        documentDto.setStatus(docStatusDto);
        documentDto.setName("Some document");
        
        SomeDocument document = mapperFacade.map(documentDto, SomeDocument.class);
        assertNotNull(document.getStatus());
        assertNotNull(document.getStatus().getDocType());
        assertEquals(new Long(1), document.getStatus().getDocType().getId());
        
        documentDto = mapperFacade.map(document, SomeDocumentDto.class);
        assertNotNull(documentDto.getStatus());
        assertEquals(new Long(1), documentDto.getStatus().getDocTypeId());
    }
    
    @Test
    public void testMapEntityToDto() {
        DocType docType = new DocType();
        docType.setId(1L);
        docType.setCaption("Test document type");
        
        DocStatus docStatus = new DocStatus();
        docStatus.setId(1L);
        docStatus.setDocType(docType);
        docStatus.setCaption("Draft");
        
        SomeDocument document = new SomeDocument();
        document.setType(docType);
        document.setStatus(docStatus);
        document.setName("Some document");
        
        SomeDocumentDto documentDto = mapperFacade.map(document, SomeDocumentDto.class);
        assertNotNull(documentDto.getStatus());
        assertEquals(new Long(1), documentDto.getStatus().getDocTypeId());
        
        document = mapperFacade.map(documentDto, SomeDocument.class);
        assertNotNull(document.getStatus());
        assertNotNull(document.getStatus().getDocType());
        assertEquals(new Long(1), document.getStatus().getDocType().getId());
    }
    
    /***********************************************************************************
     * Entities
     **********************************************************************************/
    
    public static class DocType {
        private Long id;
        private String caption;
        
        public DocType() {
        }
        
        public DocType(DocType type) {
            setId(type.getId());
            setCaption(type.getCaption());
        }
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class DocStatus {
        private Long id;
        private String caption;
        private DocType docType;
        
        public DocStatus() {
        }
        
        public DocStatus(DocStatus status) {
            setId(status.getId());
            setCaption(status.getCaption());
            setDocType(new DocType(status.getDocType()));
        }
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
        
        public DocType getDocType() {
            return docType;
        }
        
        public void setDocType(DocType docType) {
            this.docType = docType;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class SomeDocument {
        private Long id;
        private DocType type;
        private DocStatus status;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public DocStatus getStatus() {
            return status;
        }
        
        public void setStatus(DocStatus status) {
            this.status = status;
        }
        
        public DocType getType() {
            return type;
        }
        
        public void setType(DocType type) {
            this.type = type;
        }
    }
    
    /***********************************************************************************
     * DTOs
     **********************************************************************************/
    
    public static class DocTypeDto {
        private Long id;
        private String caption;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
    }
    
    public static class DocStatusDto {
        private Long id;
        private String caption;
        private Long docTypeId;
        
        public String getCaption() {
            return caption;
        }
        
        public void setCaption(String caption) {
            this.caption = caption;
        }
        
        public Long getDocTypeId() {
            return docTypeId;
        }
        
        public void setDocTypeId(Long docTypeId) {
            this.docTypeId = docTypeId;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class SomeDocumentDto {
        private Long id;
        private DocTypeDto type;
        private DocStatusDto status;
        private String name;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public DocStatusDto getStatus() {
            return status;
        }
        
        public void setStatus(DocStatusDto status) {
            this.status = status;
        }
        
        public DocTypeDto getType() {
            return type;
        }
        
        public void setType(DocTypeDto type) {
            this.type = type;
        }
    }
    
    /***********************************************************************************
     * Factory & builder
     **********************************************************************************/
    
    private static class MyClassMapBuilderFactory extends ClassMapBuilderFactory {
        @Override
        protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory,
                PropertyResolverStrategy propertyResolver, DefaultFieldMapper[] defaults) {
            return new MyClassMapBuilder<A, B>(aType, bType, mapperFactory, propertyResolver, defaults);
        }
    }
    
    private static class MyClassMapBuilder<A, B> extends ClassMapBuilder<A, B> {
        public MyClassMapBuilder(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver,
                DefaultFieldMapper... defaults) {
            super(aType, bType, mapperFactory, propertyResolver, defaults);
        }
        
        @Override
        public ClassMapBuilder<A, B> byDefault(DefaultFieldMapper... withDefaults) {
            super.byDefault(withDefaults);
            
            if (getAType().getRawType().equals(DocStatus.class)) {
                fieldMap("docType.id", "docTypeId").add();
            } else if (getAType().getRawType().equals(DocStatusDto.class)) {
                fieldMap("docTypeId", "docType.id").add();
            }
            
            return this;
        }
    }
}
