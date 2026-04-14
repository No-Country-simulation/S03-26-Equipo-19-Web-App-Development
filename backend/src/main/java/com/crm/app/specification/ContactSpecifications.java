package com.crm.app.specification;

import com.crm.app.model.Contact;
import com.crm.app.model.Tag;
import com.crm.app.model.User;
import com.crm.app.model.enums.Channel;
import com.crm.app.model.enums.FunnelStatus;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ContactSpecifications {

    public static Specification<Contact> byFunnelStatus(FunnelStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("funnelStatus"), status);
    }

    public static Specification<Contact> byOwnerId(Long ownerId) {
        return (root, query, cb) -> ownerId == null ? cb.conjunction() : cb.equal(root.get("owner").get("id"), ownerId);
    }

    public static Specification<Contact> byPreferredChannel(Channel channel) {
        return (root, query, cb) -> channel == null ? cb.conjunction() : cb.equal(root.get("preferredChannel"), channel);
    }

    public static Specification<Contact> byTagIds(List<Long> tagIds) {
        return (root, query, cb) -> {
            if (tagIds == null || tagIds.isEmpty()) return cb.conjunction();
            // Evitar resultados duplicados por el join con tags
            query.distinct(true);

            Join<Contact, Tag> tags = root.join("tags");
            return tags.get("id").in(tagIds);
        };
    }
}