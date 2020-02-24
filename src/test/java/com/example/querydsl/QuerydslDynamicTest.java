package com.example.querydsl;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslDynamicTest {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    QMember member = QMember.member;

    @BeforeEach
    public void before() {

        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        Member member5 = new Member(null, 50, teamB);
        Member member6 = new Member("park", 60, teamB);
        Member member7 = new Member("pen", 70, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
    }

    @Test
    public void dynamicQueryTest() throws Exception {

        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> members = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(members.size()).isEqualTo(1);
    }

    @Test
    public void dynamicQueryTest2() throws Exception {
        String usernameParam = "member2";
        Integer ageParam = 20;

        List<Member> members2 = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(members2.size()).isEqualTo(1);
        Assertions.assertThat(members2).extracting(Member::getUsername).containsExactly(usernameParam);
        Assertions.assertThat(members2).extracting(Member::getAge).containsExactly(ageParam);
    }

    @Test
    public void dynamicQueryTest3() throws Exception {
        String usernameParam = "member3";
        Integer ageParam = null;

        List<Member> members2 = searchMember3(usernameParam, ageParam);
        Assertions.assertThat(members2.size()).isEqualTo(1);
        Assertions.assertThat(members2).extracting(Member::getUsername).containsExactly(usernameParam);
//        Assertions.assertThat(members2).extracting(Member::getAge).containsExactly(ageParam);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private List<Member> searchMember3(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
}