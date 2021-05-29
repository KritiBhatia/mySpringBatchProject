package com.example.demo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class DemoApplication {

	@Autowired
	public JobBuilderFactory jbf;

	@Autowired
	public StepBuilderFactory sbf;

	@Bean
	public Step networkFailed() {
		return this.sbf.get("networkFailed").tasklet(new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Please upload the video again.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step promoteVideo() {
		return this.sbf.get("promoteVideo").tasklet(new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("The video has been promoted to various social media platforms.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step uploadVideo() {
		boolean upload_Failed = false;
		return this.sbf.get("uploadVideo").tasklet(new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if (upload_Failed) {
					throw new RuntimeException("Video upload failed due to network error.");
				}
				System.out.println("The video has been uploaded successfully. It is ready to be watched.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step createVideo() {

		return this.sbf.get("createVideo").tasklet(new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

				System.out.println("Video has been created to get uploaded.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step prepareContent() {
		return this.sbf.get("prepareContent").tasklet(new Tasklet() {

			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				String video = chunkContext.getStepContext().getJobParameters().get("video").toString();
				String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

				System.out.println(String.format("The content titled '%s' has been created on %s.", video, date));
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Job createYouTubeVideo() {
		return this.jbf.get("createYouTubeVideo").start(prepareContent()).next(createVideo()).next(uploadVideo())
				.on("FAILED").to(networkFailed()).from(uploadVideo()).on("*").to(promoteVideo()).end().build();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
