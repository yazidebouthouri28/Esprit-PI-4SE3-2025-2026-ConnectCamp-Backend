package tn.esprit.projetintegre.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.projetintegre.entities.Order;
import tn.esprit.projetintegre.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"user"}) // ✅ Supprimé "site" et "promotion" (site n'existe pas non plus)
    Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    Optional<Order> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"user"}) // ✅ Corrigé
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED'")
    java.math.BigDecimal getTotalRevenue();
}