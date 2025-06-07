package ru.yandex.simple_shop.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderItemEntity {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @ToString.Exclude
    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    private Integer quantity;

}