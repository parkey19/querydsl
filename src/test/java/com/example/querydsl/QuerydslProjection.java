package com.example.querydsl;

import com.example.querydsl.dto.MemberDto;
import com.example.querydsl.dto.UserDto;
import com.example.querydsl.entity.Member;
import com.example.querydsl.entity.QMember;
import com.example.querydsl.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
public class QuerydslProjection {
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
    public void tupleWayTest() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }
    }

    @Test
    public void purejpaWayTest() throws Exception {
        List<MemberDto> result = em.createQuery("select new com.example.querydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m", MemberDto.class).getResultList();
        result.stream().forEach(System.out::println);
    }

    /**
     * 결과를 DTO 반환할 때 사용 다음 3가지 방법 지원
     * 1.프로퍼티 접근
     * 2.필드 직접 접근
     * 3.생성자 사용
     */

    /**
     *  1.프로퍼티 setter 접근
     * @throws Exception
     */
    @Test
    public void propetiesWayTest() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        result.stream().forEach(System.out::println);
    }

    /**
     *  1.필드 직접 접근
     * @throws Exception
     */
    @Test
    public void filedWayTest() throws Exception {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                    member.username,
                    member.age))
                .from(member)
                .fetch();
        result.stream().forEach(System.out::println);
    }

    /**
     * 별칭 다를시
     * @throws Exception
     */
    @Test
    public void filedNameNotSame() throws Exception {
        QMember memberSub = QMember.member;
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name")
                        , ExpressionUtils.as(
                                JPAExpressions.select(memberSub.age.max())
                                        .from(memberSub), "age")
                ))
                .from(member)
                .fetch();
        fetch.stream().forEach(System.out::println);
    }

}
