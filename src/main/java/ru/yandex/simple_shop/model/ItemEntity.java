package ru.yandex.simple_shop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Table(name = "items")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
public class ItemEntity {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    private String title;

    private String description;

    @Column("img_path")
    private String imgPath;

    private Double price;

    @Transient
    @Builder.Default
    private Integer count = 0;
}