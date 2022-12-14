package com.gk.BookTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.gk.BookTracker.author.Author;
import com.gk.BookTracker.author.AuthorRepository;
import com.gk.BookTracker.book.Book;
import com.gk.BookTracker.book.BookRepository;
import com.gk.connection.DataStaxAstraProperties;


@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BookTrackerApplication {
	
	@Autowired
	AuthorRepository authorRepository;
	
	@Autowired
	BookRepository bookRepository;
	
	@Value("${datadump.location.author}")
	private String authorDumpLocation;
	
	@Value("${datadump.location.works}")
	private String worksDumpLocation;
	
	public static void main(String[] args) {
		SpringApplication.run(BookTrackerApplication.class, args);
	}
	
	private void initAuthors() {
		Path path = Paths.get(authorDumpLocation);
		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				//Read and parse the line
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject jsonObject =  new JSONObject(jsonString);
					
					//construct Author object
					Author author = new Author();
					System.out.println(jsonObject.optString("key"));
					author.setId(jsonObject.optString("key").replace("/authors/", ""));
					author.setName(jsonObject.optString("name"));
					author.setPersonalName(jsonObject.optString("personal_name"));
					
					//Persist using repository
					System.out.println("Saving author "+author.getName()+"...");
					authorRepository.save(author);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			});
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initWorks() {
		Path path = Paths.get(worksDumpLocation);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				//Read and parse the line
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject jsonObject =  new JSONObject(jsonString);
					
					//construct Author object
					Book book = new Book();
					book.setId(jsonObject.optString("key").replace("/works/", ""));
					book.setName(jsonObject.optString("title"));
					JSONObject descriptionObject = jsonObject.optJSONObject("description");
					if(descriptionObject!=null)
					{
						book.setDescription(descriptionObject.optString("value"));
					}
					JSONObject publishedObject = jsonObject.optJSONObject("created");
					if(publishedObject!=null)
					{
						String dateString =publishedObject.optString("value");
						book.setPublishedDate(LocalDate.parse(dateString,dateTimeFormatter));
					}
					JSONArray coversJSONArray = jsonObject.optJSONArray("covers");
					if(coversJSONArray!= null)
					{
						List<String> coverIds = new ArrayList();
						for(int i=0; i<coversJSONArray.length();i++)
						{
							coverIds.add(String.valueOf(coversJSONArray.getInt(i)));
						}
						book.setCoverIds(coverIds);
					}
					
					JSONArray authorsJsonArray = jsonObject.optJSONArray("authors");
					List<String> authorIds = new ArrayList<String>();
					if(authorsJsonArray!= null)
					{
						for(int i=0; i<authorsJsonArray.length();i++)
						{
							authorIds.add(authorsJsonArray.optJSONObject(i).getJSONObject("author").getString("key").replace("/authors/", ""));
						}
						book.setAuthorIds(authorIds);
					}
					
					List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id)).
							map(optionalAuthor -> {
								if(!optionalAuthor.isPresent()) return "Unknown Author";
								return optionalAuthor.get().getName();
							}).collect(Collectors.toList());
					book.setAuthorNames(authorNames);
					
					//Persist using repository
					System.out.println("Saving book "+book.getName()+"...");
					bookRepository.save(book);
					
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			});
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@PostConstruct
	public void start()
	{
		//initWorks();
	}
	
	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties)
	{
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		 return builder -> builder.withCloudSecureConnectBundle(bundle);
	}
	

}
