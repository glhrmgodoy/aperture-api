package com.godoy.aperture.domain.entity;

import com.godoy.aperture.domain.enums.Genre;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movies")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer releaseYear;

    @Column(nullable = false)
    private String director;

    @Column(nullable = false, length = 600)
    private String synopsis;

    @Column(nullable = false)
    private String posterUrl;

    @Column(nullable = false)
    private Integer runtimeMinutes;

    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private List<Genre> genres;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "movie")
    private List<Review> reviews;

    @OneToMany(mappedBy = "movie")
    private List<ListItem> listItems;

    @OneToMany(mappedBy = "movie")
    private List<WatchList> watchLists;
}
