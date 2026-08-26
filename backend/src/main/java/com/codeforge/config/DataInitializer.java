package com.codeforge.config;

import com.codeforge.common.enums.ProblemDifficulty;
import com.codeforge.module.problem.entity.Problem;
import com.codeforge.module.problem.entity.ProblemTestCase;
import com.codeforge.module.problem.entity.Tag;
import com.codeforge.module.problem.repository.ProblemRepository;
import com.codeforge.module.problem.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (problemRepository.count() > 0) {
            return;
        }

        log.info("Seeding initial problem catalog and algorithmic tags...");

        Tag arrayTag = tagRepository.save(Tag.builder().name("Array").slug("array").build());
        Tag hashTag = tagRepository.save(Tag.builder().name("Hash Table").slug("hash-table").build());
        Tag stringTag = tagRepository.save(Tag.builder().name("String").slug("string").build());
        Tag twoPointerTag = tagRepository.save(Tag.builder().name("Two Pointers").slug("two-pointers").build());

        // 1. Two Sum
        Problem twoSum = Problem.builder()
                .slug("two-sum")
                .title("Two Sum")
                .description("""
                        Given an array of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.

                        You may assume that each input would have exactly one solution, and you may not use the same element twice.

                        ### Input Format:
                        - First line: integer `n` (number of elements)
                        - Second line: `n` space-separated integers
                        - Third line: integer `target`

                        ### Output Format:
                        - Space-separated indices of the two elements (0-indexed).

                        ### Example 1:
                        ```
                        Input:
                        4
                        2 7 11 15
                        9
                        Output:
                        0 1
                        ```
                        """)
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .isPublished(true)
                .tags(new HashSet<>(Set.of(arrayTag, hashTag)))
                .testCases(new ArrayList<>())
                .build();

        ProblemTestCase ts1 = ProblemTestCase.builder()
                .problem(twoSum)
                .inputData("4\n2 7 11 15\n9")
                .expectedOutput("0 1")
                .isSample(true)
                .explanation("nums[0] + nums[1] = 2 + 7 = 9")
                .orderIndex(1)
                .build();

        ProblemTestCase ts2 = ProblemTestCase.builder()
                .problem(twoSum)
                .inputData("3\n3 2 4\n6")
                .expectedOutput("1 2")
                .isSample(true)
                .explanation("nums[1] + nums[2] = 2 + 4 = 6")
                .orderIndex(2)
                .build();

        ProblemTestCase ts3 = ProblemTestCase.builder()
                .problem(twoSum)
                .inputData("2\n3 3\n6")
                .expectedOutput("0 1")
                .isSample(false)
                .orderIndex(3)
                .build();

        twoSum.getTestCases().addAll(List.of(ts1, ts2, ts3));
        problemRepository.save(twoSum);

        // 2. Reverse String
        Problem reverseString = Problem.builder()
                .slug("reverse-string")
                .title("Reverse String")
                .description("""
                        Write a program that takes a string and prints the reversed string.

                        ### Input Format:
                        - A single string `s`.

                        ### Output Format:
                        - The reversed string.

                        ### Example 1:
                        ```
                        Input:
                        hello
                        Output:
                        olleh
                        ```
                        """)
                .difficulty(ProblemDifficulty.EASY)
                .timeLimitMs(1000)
                .memoryLimitMb(256)
                .isPublished(true)
                .tags(new HashSet<>(Set.of(stringTag, twoPointerTag)))
                .testCases(new ArrayList<>())
                .build();

        ProblemTestCase rs1 = ProblemTestCase.builder()
                .problem(reverseString)
                .inputData("hello")
                .expectedOutput("olleh")
                .isSample(true)
                .orderIndex(1)
                .build();

        ProblemTestCase rs2 = ProblemTestCase.builder()
                .problem(reverseString)
                .inputData("CodeForge")
                .expectedOutput("egroFedoC")
                .isSample(false)
                .orderIndex(2)
                .build();

        reverseString.getTestCases().addAll(List.of(rs1, rs2));
        problemRepository.save(reverseString);

        log.info("Initialized 2 starter coding problems with sample and judge test cases.");
    }
}
