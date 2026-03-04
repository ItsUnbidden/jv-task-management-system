package com.unbidden.jvtaskmanagementsystem.model;

import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE messages SET is_deleted = true WHERE id = ?")
public class Reply extends Message {
    @ManyToOne()
    @JoinColumn(name = "parent_id")
    private Message parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @SQLRestriction("is_deleted <> true")
    private List<Reply> replies;
}
