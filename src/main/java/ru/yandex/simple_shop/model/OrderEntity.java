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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderEntity {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("total_price")
    private Double totalPrice;

    @Builder.Default
    @Transient
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Transient
    private List<ItemEntity> items = new ArrayList<>();

}
