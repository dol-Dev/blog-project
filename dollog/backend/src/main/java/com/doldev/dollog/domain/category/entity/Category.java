package com.doldev.dollog.domain.category.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    // orderIndex -> 부모 카테고리 정렬용 필드
    @Column(name = "parent_order_index")
    private Integer parentOrderIndex;

    // orderIndex -> 자식 카테고리 정렬용 필드
    @Column(name = "child_order_index")
    private Integer childOrderIndex;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "category", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    // 게시물 수 계산
    @JsonGetter("postCount")
    public int getPostCount() {
        return posts.size();
    }

    // 메서드 추가
    public void changeName(String name) {
        this.name = name;
    }

    public void assignParent(Category parent) {
        this.parent = parent;
    }

    public void addChild(Category child) {
        this.children.add(child);
        child.assignParent(this);
    }

    public void changeParentOrderIndex(int index) {
        this.parentOrderIndex = index;
    }

    public void changeChildOrderIndex(int index) {
        this.childOrderIndex = index;
    }

    public void assignUser(User user) {
        this.user = user;
    }
}
