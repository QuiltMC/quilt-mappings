package quilt.internal.taglet;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class MappingTaglet implements Taglet {
    // Required by javadoc, see TagletManager#addCustomTag
    public MappingTaglet() {}

    @Override
    public Set<Location> getAllowedLocations() {
        return EnumSet.of(Location.CONSTRUCTOR, Location.FIELD, Location.METHOD, Location.TYPE);
    }

    @Override
    public boolean isInlineTag() {
        return false;
    }

    @Override
    public String getName() {
        return "mapping";
    }

    @Override
    public String toString(List<? extends DocTree> tags, Element element) {
        return null;
    }
}
