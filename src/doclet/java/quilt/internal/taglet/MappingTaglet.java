/*
 * This file is free for everyone to use under the Creative Commons Zero license.
 */

package quilt.internal.taglet;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        StringBuilder content = new StringBuilder();
        content.append("""
                <dt>Mappings:</dt>
                <dd><div class="quilt"><table class="mapping" summary="Mapping data">
                    <thead>
                        <th>Namespace</th>
                        <th>Name</th>
                    </thead>
                    <tbody>
                """);

        for (DocTree tag : tags) {
            String body = ((UnknownBlockTagTree) tag).getContent().stream().map(t -> ((LiteralTree) t).getBody().getBody()).collect(Collectors.joining());
            String[] split = body.split(" +", 2);
            String namespace = split[0];
            String name = split[1];

            content.append(String.format("""
                            <tr>
                                <td>%s</td>
                                <td><span class="copyable"><code>%s</code></span></td>
                            </tr>
                    """, namespace, name));
        }

        content.append("""
                    </tbody>
                    </table></div>
                </dd>
                """);

        return content.toString();
    }
}
