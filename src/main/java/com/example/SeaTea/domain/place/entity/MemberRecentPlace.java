package com.example.SeaTea.domain.place.entity;

import com.example.SeaTea.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "member_recent_place", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "place_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRecentPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_recent_place_id")
    private Long memberRecentPlaceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    private MemberRecentPlace(Place place, Member member) {
        this.place = place;
        this.member = member;
        this.viewedAt = LocalDateTime.now();
    }

    public void touchViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }

    public static MemberRecentPlace of(Member member, Place place) {
        return new MemberRecentPlace(place, member);
    }
}
