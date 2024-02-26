package com.example.titto_backend.questionBoard.service;

import com.example.titto_backend.auth.domain.User;
import com.example.titto_backend.auth.repository.UserRepository;
import com.example.titto_backend.auth.service.ExperienceService;
import com.example.titto_backend.common.exception.CustomException;
import com.example.titto_backend.common.exception.ErrorCode;
import com.example.titto_backend.questionBoard.domain.Department;
import com.example.titto_backend.questionBoard.domain.Question;
import com.example.titto_backend.questionBoard.domain.Status;
import com.example.titto_backend.questionBoard.dto.QuestionDTO;
import com.example.titto_backend.questionBoard.dto.QuestionDTO.Response;
import com.example.titto_backend.questionBoard.repository.AnswerRepository;
import com.example.titto_backend.questionBoard.repository.QuestionRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final AnswerRepository answerRepository;

    private final ExperienceService experienceService;

    //Create
    @Transactional
    public String save(String email, QuestionDTO.Request request) throws CustomException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getSendExperience() > user.getTotalExperience()) throw new CustomException(ErrorCode.INSUFFICIENT_EXPERIENCE);

        experienceService.deductExperience(user, request.getSendExperience()); // 유저 경험치 차감

        questionRepository.save(Question.builder()
                .title(request.getTitle())
                .author(user)
                .content(request.getContent())
                .department(Department.valueOf(request.getDepartment().toUpperCase()))
                .status(Status.valueOf(request.getStatus().toUpperCase()))
                .sendExperience(request.getSendExperience())
                .viewCount(0)
                .isAnswerAccepted(false)
                .build());
        return "질문이 성공적으로 등록되었습니다.";
    }

    @Transactional(readOnly = true)
    public Page<Response> findAll(Pageable pageable) {
        return questionRepository.findAllByOrderByCreateDateDesc(pageable).map(QuestionDTO.Response::new);
    }

    @Transactional
    public Response findById(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Question question = questionRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
        validViewCount(question, request, response);
        return new Response(question);
    }

    @Transactional(readOnly = true)
    public Page<QuestionDTO.Response> findByCategory(Pageable pageable, String category) {
        return questionRepository.findByDepartmentOrderByCreateDateDesc(pageable,
                        Department.valueOf(category.toUpperCase()))
                .map(QuestionDTO.Response::new);
    }

    // Update
    @Transactional
    public void update(QuestionDTO.Update update, Long id, Long userId) throws CustomException {
        validateAuthorIsLoggedInUser(id, userId);
        Question oldQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
        oldQuestion.update(
                update.getTitle(),
                update.getContent(),
                Department.valueOf(String.valueOf(update.getDepartment())),
                Status.valueOf(String.valueOf(update.getStatus()))
        );
    }

    // Delete
    @Transactional
    public void delete(Long id, Long userId) {
        validateAuthorIsLoggedInUser(id, userId);
        questionRepository.deleteById(id);
    }

    // 질문 게시판에 채택된 답변이 없으면 경험치 되돌려줌, 채택된 답변 있을 시 삭제 불가능
    private void isAcceptAnswer(Question question, User user) {
        if (!question.isAnswerAccepted()) {
            user.setCurrentExperience(user.getCurrentExperience() + question.getSendExperience());
        } else {
            throw new CustomException(ErrorCode.DELETE_NOT_ALLOWED);
        }
    }

    // 글을 쓴 사람과 현재 로그인한 사람이 같은지 확인
    private void validateAuthorIsLoggedInUser(Long id, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));

        isAcceptAnswer(question, user);

        answerRepository.deleteAllByQuestion(question);
        if (!question.getAuthor().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.MISMATCH_AUTHOR);
        }
    }

    // 조회수 증가
    private void validViewCount(Question question, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        System.out.println("cookies = " + Arrays.toString(cookies));
        Cookie cookie = null;
        boolean isCookie = false;
        for (Cookie value : cookies) {
            if (value.getName().equals("viewCookie")) {
                cookie = value;
                if (!cookie.getValue().contains("[" + question.getId() + "]")) {
                    question.addViewCount();
                    cookie.setValue(cookie.getValue() + "[" + question.getId() + "]");
                }
                isCookie = true;
                break;
            }
        }
        System.out.println("isCookie = " + isCookie);
        if (!isCookie) {
            question.addViewCount();
            cookie = new Cookie("viewCookie", "[" + question.getId() + "]");
        }
        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        cookie.setPath("/");
        cookie.setMaxAge((int) (todayEndSecond - currentSecond));
        response.addCookie(cookie);
    }

}
