package ru.app.voicechat.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "recordings")
@Getter
@Setter
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name = null;

    @Lob
    @Column
    private String content;

    @OneToOne
    private User owner;

    private Boolean privateToOwner;
}