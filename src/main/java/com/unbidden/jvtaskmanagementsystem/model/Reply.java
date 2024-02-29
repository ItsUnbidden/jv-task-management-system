package com.unbidden.jvtaskmanagementsystem.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLDelete;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE messages SET is_deleted = true WHERE id = ?")
public class Reply extends Message {
    @ManyToOne()
    @JoinColumn(name = "parent_id")
    private Message parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private List<Reply> replies;
}
