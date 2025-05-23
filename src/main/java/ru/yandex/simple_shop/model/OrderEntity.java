package ru.yandex.simple_shop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "orders")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "total_price")
    private Double totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @Transient
    public List<ItemEntity> getItems() {
        List<ItemEntity> items = new ArrayList<>();
        for (OrderItemEntity orderItem : orderItems) {
            ItemEntity item = orderItem.getItem();
            item.setCount(orderItem.getQuantity());
            items.add(item);
        }
        return items;
    }
}
