package org.drugis.trialverse.core;

import java.net.URI;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Identifiable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class EntityLinkResolver {
	private static EntityLinkResolver INSTANCE;
	@Autowired EntityLinks d_links;

	@PostConstruct
	private void setStaticInstance() {
		INSTANCE = this;
	}

	public static EntityLinkResolver getInstance() {
		return INSTANCE;
	}

	public URI getLinkForEntity(final Identifiable<?> entity) {
		return entity != null ? d_links.linkForSingleResource(entity).toUri() : null;
	}

	public URI getLinkForEntity(final Class<?> type, final Object id) {
		Assert.notNull(type);
		Assert.notNull(id);
		return d_links.linkForSingleResource(type, id).toUri();
	}
}
