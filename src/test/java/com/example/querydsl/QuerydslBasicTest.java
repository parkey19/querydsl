package com.example.querydsl;

import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static com.example.querydsl.entity.QTeam.team;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

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
    public void startJPQL() {

        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");

    }
        @Test
    public void startQuerydsl() {
    //member1을 찾아라.

            QMember m = new QMember("m");
            Member findMember = queryFactory.select(m)
                    .from(m)
                    .where(m.username.eq("member1"))//파라미터 바인딩 처리
                    .fetchOne();
            assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl3() {
        //member1을 찾아라.

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void isNotNullTest() {
        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .where(member.username.isNotNull())
                .fetch();

        assertThat(findMembers.size()).isEqualTo(6);
    }

    @Test
    public void isNullTest() {
        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .where(member.username.isNull())
                .fetch();

        assertThat(findMembers.size()).isEqualTo(1);
    }

    @Test
    public void likeTest() {
        List<Member> findMembers = queryFactory
                .selectFrom(member)
                .where(member.username.like("p%"))
                .fetch();

        assertThat(findMembers.size()).isEqualTo(2);
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);

        assertThat(member5.getUsername()).isNull();
        assertThat(member5.getAge()).isEqualTo(100);

    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc()).offset(1) //0부터 시작(zero index) .limit(2) //최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(6);
    }

    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(7);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(7);
        assertThat(tuple.get(member.age.sum())).isEqualTo(280);
        assertThat(tuple.get(member.age.avg())).isEqualTo(40);
        assertThat(tuple.get(member.age.max())).isEqualTo(70);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(50);
    }

}
