import lombok.Data;

/**
 * @author Nami
 * @date 05.06.2023
 * @time 20:32
 */
@Data
public class TestClass {
    double skillId;
    double skillLevel;
    double hitTime;
    double reuseDelay;

    public TestClass(double skillId, double skillLevel, double hitTime, double reuseDelay) {
        this.skillId = skillId;
        this.skillLevel = skillLevel;
        this.hitTime = hitTime;
        this.reuseDelay = reuseDelay;
    }
}
