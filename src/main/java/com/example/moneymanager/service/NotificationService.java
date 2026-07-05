package com.example.moneymanager.service;

import com.example.moneymanager.dto.ExpenseDto;
import com.example.moneymanager.entity.Profile;
import com.example.moneymanager.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final ExpenseService expenseService;

    @Value("${money.manager.frontend.url}")
    private String frontendUrl;

    @Scheduled(cron = "0 0 22 * * *", zone = "IST")
    public void sendNotification(){
        log.info("Starting scheduled task to send notifications to users with high expenses.");
        List<Profile> profileList = profileRepository.findAll();

        for (Profile profile : profileList){
            String body = "Dear " + profile.getFullname() + ",\n\n" +
                    "This is your daily reminder to log your income and expenses for today. " +
                    "Keeping your records up-to-date helps you stay on track with your financial goals!\n\n" +
                    "You can quickly add your transactions and view your dashboard here: " + frontendUrl + "\n\n" +
                    "Best regards,\n" +
                    "Money Manager Team";

            emailService.sendEmail(profile.getEmail(), "Daily Reminder: Log Your Income and Expenses", body);
        }
    }

    @Scheduled(cron = "0 0 23 * * *", zone = "IST")
    public void sendDailyExpenses(){
        log.info("job started: sendDailyExpenses()");
        List<Profile> profiles = profileRepository.findAll();

        for (Profile profile : profiles) {
            List<ExpenseDto> expenses = expenseService.getTodayExpenses(profile.getId(), LocalDate.now());
            if(!expenses.isEmpty()){
                StringBuilder table = new StringBuilder();
                table.append("<html>")
                        .append("<body>")
                        .append("<h2>Daily Expenses Report</h2>")
                        .append("<table border='1' cellpadding='5' cellspacing='0' style='border-collapse: collapse;'>")
                        .append("<tr style='background-color:#f2f2f2;'>")
                        .append("<th>Name</th>")
                        .append("<th>Category</th>")
                        .append("<th>Amount</th>")
                        .append("</tr>");
               for (ExpenseDto expenseDto : expenses){
                     table.append("<tr>")
                            .append("<td>").append(expenseDto.getName()).append("</td>")
                            .append("<td>").append(expenseDto.getCategoryName()).append("</td>")
                            .append("<td>").append(expenseDto.getAmount()).append("</td>")
                            .append("</tr>");
               }

               table.append("</table>")
                       .append("</body>")
                       .append("</html>");

                String body = "Dear " + profile.getFullname() + ",\n\n" +
                        "Here is your daily expenses report for today:\n\n" +
                        table.toString() + "\n\n" +
                        "Best regards,\n" +
                        "Money Manager Team";

                emailService.sendEmail(profile.getEmail(), "Your Daily Expenses Report", body);
            }
        }
        log.info("job ended: sendDailyExpenses()");
    }
}
