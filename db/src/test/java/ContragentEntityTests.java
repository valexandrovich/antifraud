import org.junit.Test;
import ua.com.solidity.db.entities.ContragentEntity;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ContragentEntityTests {
//    @Test
//    void contragentEntityCouldBeConstructedTest() {
//        ContragentEntity contragentEntity = new ContragentEntity(
//                984270,
//                "БЄЛОЄНКО ВАЛЕРІЙ ОЛЕКСАНДРОВИЧ",
//                5,
//                99,
//                804,
//                10,
//                "БЄЛОЄНКО В. О.",
//                99999,
//                14430,
//                3435603818,
//                0,
//                0,
//                2,
//                0,
//                "00000, м. КИЇВ, вул. Марічанська, буд. 13, кв. 72",
//                0,
//                0,
//                0,
//                0,
//                0,
//                0,
//                "BIEVZXOMY",
//                0,
//                0,
//                0,
//                0,
//                2,
//                "VALERII BIELOIENKO",
//                "TYSIACHNAB",
//                0,
//                new LocalDate(2013,11, 29),
//                0,
//                "00000, ГОРЛІВКА, вул. Магістральна, буд. 7, кв. 311",
//                0,
//                0,
//                0,
//                0,
//                0,
//                950000,
//                "1/VALERII BIELOIENKO",
//                "2/VUL. MAHISTRALNA,7,",
//                "3/UA/MISTO HORLIVKA",
//                0,
//                0,
//                5,
//                0,
//                804,
//                "00000",
//                0,
//                0,
//                "КИЇВ",
//                "Марічанська",
//                13,
//                72,
//                804,
//                "00000",
//                "ДОНЕЦЬКА ОБЛАСТЬ",
//                0,
//                "ГОРЛІВКА",
//                "Магістральна",
//                7,
//                311,
//                0,
//                2481771,
//                300528,
//                0,
//                1,
//                0,
//                0,
//                0,
//                3
//        );
////        assertEquals(1L, contragentEntity.getId());
//    }

    @Test
    public void emptyContragentEntitiesAreNotEqualTest() {
        ContragentEntity contragentEntity1 = new ContragentEntity();
        ContragentEntity contragentEntity2 = new ContragentEntity();
        assertNotEquals(contragentEntity1, contragentEntity2);
    }
}
