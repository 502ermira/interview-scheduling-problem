public class TimeSlot {
    public int companyId;
    public int day;
    public int timeSlot;
    public boolean available = true;
    public TimeSlot(int companyId, int day, int timeSlot) {
        this.companyId = companyId;
        this.day = day;
        this.timeSlot = timeSlot;
    }
}