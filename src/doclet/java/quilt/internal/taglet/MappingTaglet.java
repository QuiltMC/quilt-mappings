/*
 * This file is free for everyone to use under the Creative Commons Zero license.
 */

package quilt.internal.taglet;

import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import jdk.javadoc.doclet.Taglet;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
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
        boolean elementIsType = element instanceof TypeElement;

        StringBuilder content = new StringBuilder();
        content.append(String.format("""
                <dt>Mappings:</dt>
                <dd><div class="quilt"><table class="mapping" summary="Mapping data">
                    <thead>
                        <th>Namespace</th>
                        <th>Name</th>%s
                    </thead>
                    <tbody>
                """,
                elementIsType ? "" : "\n<th>Mixin selector</th>"));

        for (DocTree tag : tags) {
            String body = ((UnknownBlockTagTree) tag).getContent().stream().map(t -> ((LiteralTree) t).getBody().getBody()).collect(Collectors.joining());
            String[] split = body.split(" +", 3);
            String namespace = escapeHtml(split[0]);
            String name = escapeHtml(split[1]);
            String selector = elementIsType ? "" : escapeHtml(split[2]);

            content.append(String.format("""
                            <tr>
                                <td>%s</td>
                                <td><span class="copyable"><code>%s</code></span></td>%s
                            </tr>
                    """, namespace, name,
                    elementIsType ? "" : String.format("<td><span class=\"copyable\"><code>%s</code></span></td>", selector)));
        }

        content.append("""
                    </tbody>
                    </table></div>
                </dd>
                """);

        return content.toString();
    }

    private static String escapeHtml(String s) {
        // Escape html characters
        StringBuilder result = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                result.append("&#").append((int) c).append(';');
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
