package eu.domaindriven.ddq.domain;

import javax.json.bind.annotation.JsonbProperty;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.*;

@SuppressWarnings("unused")
public abstract class Representation {

    @JsonbProperty("_links")
    private final List<Link> links;
    @JsonbProperty("_embedded")
    private final Map<String, Object> embedded;

    public Representation() {
        links = new ArrayList<>();
        embedded = new LinkedHashMap<>();
    }

    protected final Representation link(Link link) {
        links.add(link);

        return this;
    }

    protected final Representation link(String relationship, UriBuilder builder, String... paths) {
        return link(createLink(relationship, builder, paths));
    }

    protected final Representation link(String relationship, URI uri) {
        return link(createLink(relationship, uri));
    }

    public List<Link> links() {
        return Collections.unmodifiableList(links);
    }

    protected static Link createLink(String relationship, UriBuilder builder, String... paths) {
        Arrays.stream(paths).forEach(builder::path);
        URI uri = builder.build();

        return createLink(relationship, uri);
    }

    protected static Link createLink(String relationship, URI uri) {
        return Link.fromUri(uri)
                .rel(relationship)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    protected final Representation embedded(String name, Representation representation) {
        embedded.put(name, representation);

        return this;
    }

    protected final Representation embedded(String name, Collection<? extends Representation> representations) {
        embedded.put(name, representations);

        return this;
    }

    public Object embedded(String name) {
        return embedded.get(name);
    }
}
