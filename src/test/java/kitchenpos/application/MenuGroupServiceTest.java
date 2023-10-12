package kitchenpos.application;

import kitchenpos.dao.MenuGroupDao;
import kitchenpos.domain.MenuGroup;
import kitchenpos.fixture.MenuGroupFixture;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import java.util.List;

import static kitchenpos.fixture.MenuGroupFixture.menuGroup;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MenuGroupServiceTest {

    @Autowired
    private MenuGroupService menuGroupService;

    @Autowired
    private MenuGroupDao menuGroupDao;

    @Test
    @DisplayName("메뉴 그룹을 등록한다")
    void create() {
        // given
        final MenuGroup menuGroup = menuGroup("추천메뉴");

        // when
        final MenuGroup actual = menuGroupService.create(menuGroup);

        // then
        assertThat(actual.getId()).isPositive();
    }

    @Test
    @DisplayName("메뉴 그룹 목록을 조회한다")
    void list() {
        // given
        final MenuGroup expect1 = menuGroupDao.save(menuGroup("추천메뉴"));
        final MenuGroup expect2 = menuGroupDao.save(menuGroup("신메뉴"));

        // when
        final List<MenuGroup> actual = menuGroupService.list();

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual).hasSize(2);
            softAssertions.assertThat(actual.get(0).getName()).isEqualTo(expect1.getName());
            softAssertions.assertThat(actual.get(1).getName()).isEqualTo(expect2.getName());
        });
    }
}
