import org.junit.Assert;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.OrikaSystemProperties;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.JavassistCompilerStrategy;

public class DefaultPackageTestCase {
    public static class Label {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class XmlLabel {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Test
    public void test() {

        System.setProperty(OrikaSystemProperties.COMPILER_STRATEGY,
                JavassistCompilerStrategy.class.getName());

        System.setProperty(OrikaSystemProperties.WRITE_SOURCE_FILES, "true");
        System.setProperty(OrikaSystemProperties.WRITE_CLASS_FILES, "true");

        MapperFactory factory = new DefaultMapperFactory.Builder().build();
        MapperFacade mapper = factory.getMapperFacade();

        XmlLabel xmlLabel1 = new XmlLabel();
        xmlLabel1.setText("label");

        Label label = mapper.map(xmlLabel1, Label.class);

        Assert.assertEquals(xmlLabel1.getText(), label.getText());

    }
}
