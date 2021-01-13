package nextstep.subway.line;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;

@DisplayName("지하철 구간 관련 기능")
public class SectionAcceptanceTest extends AcceptanceTest {

	private long 강남역_ID;
	private long 양재역_ID;
	private long 청계산입구역_ID;
	private long 판교역_ID;
	private long 정자역_ID;
	private long 광교역_ID;
	private long 신분당선_ID;

	@BeforeEach
	public void setUp() {
		super.setUp();

		// given
		강남역_ID = StationAcceptanceTest.지하철_생성_요청("강남역").as(StationResponse.class).getId();
		양재역_ID = StationAcceptanceTest.지하철_생성_요청("양재역").as(StationResponse.class).getId();
		청계산입구역_ID = StationAcceptanceTest.지하철_생성_요청("청계산입구역").as(StationResponse.class).getId();
		판교역_ID = StationAcceptanceTest.지하철_생성_요청("판교역").as(StationResponse.class).getId();
		정자역_ID = StationAcceptanceTest.지하철_생성_요청("정자역").as(StationResponse.class).getId();
		광교역_ID = StationAcceptanceTest.지하철_생성_요청("광교역").as(StationResponse.class).getId();

		HashMap<String, Object> createParams = new HashMap<>();
		createParams.put("name", "신분당선");
		createParams.put("color", "bg-red-600");
		createParams.put("upStationId", 양재역_ID);
		createParams.put("downStationId", 정자역_ID);
		createParams.put("distance", 10);

		신분당선_ID = LineAcceptanceTest.지하철노선_생성_요청(createParams).as(LineResponse.class).getId();
	}

	@DisplayName("역 사이에 새로운 역을 등록한다.")
	@Test
	void addSection_happyPath() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse<Response> response = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 판교역_ID, 5);

		// then : 지하철_구간_등록됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response.as(SectionResponse.class).getAddedStationId()).isEqualTo(판교역_ID)
		);
	}

	@DisplayName("역 사이에 새로운 역을 여러 개 등록한다")
	@Test
	void addSection_happyPath2() {
		// when : 지하철_구간_등록_요청 & 한번 더 요청
		ExtractableResponse response1 = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 청계산입구역_ID, 5);
		ExtractableResponse response2 = 지하철_구간_등록_요청(신분당선_ID, 청계산입구역_ID, 판교역_ID, 4);

		// then : 지하철_구간_등록됨 & 두번째도 등록됨
		assertAll(
			() -> assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response1.as(SectionResponse.class).getAddedStationId()).isEqualTo(청계산입구역_ID),
			() -> assertThat(response2.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response2.as(SectionResponse.class).getAddedStationId()).isEqualTo(판교역_ID)
		);
	}

	@DisplayName("이미 등록된 구간을 등록한다.")
	@Test
	void addSection_exceptionCase1() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 정자역_ID, 양재역_ID, 8);

		// then : 지하철_구간_등록_실패됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("최초 등록이 아니면서, 이미 등록된 역을 하나도 포함하지 않는 구간을 등록한다.")
	@Test
	void addSection_exceptionCase2() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 강남역_ID, 광교역_ID, 20);

		// then : 지하철_구간_등록_실패됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("역 사이에 새로운 역을 등록할 때, 기존 역 사이 길이보다 크게 등록한다.")
	@Test
	void addSection_exceptionCase3() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 판교역_ID, 15);

		// then : 지하철_구간_등록_실패됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("역 사이에 새로운 역을 등록할 때, 기존 역 사이 길이보다 같게 등록한다.")
	@Test
	void addSection_exceptionCase4() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 판교역_ID, 10);

		// then : 지하철_구간_등록_실패됨
		assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
	}

	@DisplayName("역 사이에 새로운 역을 여러 개 등록할 때, 기존 역 사이 길이보다 크게 등록한다.")
	@Test
	void addSection_exceptionCase5() {
		// when : 지하철_구간_등록_요청 & 한번 더 요청
		ExtractableResponse response1 = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 청계산입구역_ID, 5);
		ExtractableResponse response2 = 지하철_구간_등록_요청(신분당선_ID, 청계산입구역_ID, 판교역_ID, 6);

		// then : 지하철_구간_등록됨 & 지하철_구간_등록_실패됨
		assertAll(
			() -> assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response1.as(SectionResponse.class).getAddedStationId()).isEqualTo(청계산입구역_ID),
			() -> assertThat(response2.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
		);
	}

	@DisplayName("역 사이에 새로운 역을 여러 개 등록할 때, 기존 역 사이 길이와 같게 등록한다.")
	@Test
	void addSection_exceptionCase6() {
		// when : 지하철_구간_등록_요청 & 한번 더 요청
		ExtractableResponse response1 = 지하철_구간_등록_요청(신분당선_ID, 양재역_ID, 청계산입구역_ID, 4);
		ExtractableResponse response2 = 지하철_구간_등록_요청(신분당선_ID, 청계산입구역_ID, 판교역_ID, 6);

		// then : 지하철_구간_등록됨 & 지하철_구간_등록_실패됨
		assertAll(
			() -> assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response1.as(SectionResponse.class).getAddedStationId()).isEqualTo(청계산입구역_ID),
			() -> assertThat(response2.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
		);
	}

	@DisplayName("새로운 역을 상행 종점으로 등록한다.")
	@Test
	void addSection_newUpStation() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 강남역_ID, 양재역_ID, 3);

		// then : 지하철_구간_등록됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response.as(SectionResponse.class).getAddedStationId()).isEqualTo(강남역_ID)
		);
	}

	@DisplayName("새로운 역을 하행 종점으로 등록한다.")
	@Test
	void addSection_newDownStation() {
		// when : 지하철_구간_등록_요청
		ExtractableResponse response = 지하철_구간_등록_요청(신분당선_ID, 정자역_ID, 광교역_ID, 4);

		// then : 지하철_구간_등록됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
			() -> assertThat(response.as(SectionResponse.class).getAddedStationId()).isEqualTo(광교역_ID)
		);
	}

	@DisplayName("노선에서 종점이 포함되지 않은 구간을 제거한다")
	@Test
	void removeSection_happyPath1() {
		// given : 지하철_구간_등록_요청
		지하철_구간_등록_요청(신분당선_ID, 강남역_ID, 양재역_ID, 4);
		assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("강남역", "양재역", "정자역");

		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(신분당선_ID, 양재역_ID);

		// then : 지하철_노선_제거_응답됨 & 지하철_노선_역_이름_목록에서_제거됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
			() -> assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("강남역", "정자역")
		);
	}

	@DisplayName("노선에서 상행 종점이 포함된 구간을 제거한다")
	@Test
	void removeSection_happyPath2() {
		// given : 지하철_구간_등록_요청
		지하철_구간_등록_요청(신분당선_ID, 강남역_ID, 양재역_ID, 4);
		assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("강남역", "양재역", "정자역");

		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(신분당선_ID, 강남역_ID);

		// then : 지하철_노선_제거_응답됨 & 지하철_노선_역_이름_목록에서_제거됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
			() -> assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("양재역", "정자역")
		);
	}

	@DisplayName("노선에서 하행 종점이 포함된 구간을 제거한다")
	@Test
	void removeSection_happyPath3() {
		// given : 지하철_구간_등록_요청
		지하철_구간_등록_요청(신분당선_ID, 강남역_ID, 양재역_ID, 4);
		assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("강남역", "양재역", "정자역");

		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(신분당선_ID, 정자역_ID);

		// then : 지하철_노선_제거_응답됨 & 지하철_노선_역_이름_목록에서_제거됨
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value()),
			() -> assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("강남역", "양재역")
		);
	}

	@DisplayName("구간이 하나인 노선에서 마지막 구간을 제거한다")
	@Test
	void removeSection_exceptionCase1() {
		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(신분당선_ID, 양재역_ID);

		// then : 지하철_노선_제거_실패됨 & 지하철_노선_역_이름_목록이_요청_전과_동일
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
			() -> assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("양재역", "정자역")
		);
	}

	@DisplayName("노선에 포함되어 있지 않은 구간을 제거한다")
	@Test
	void removeSection_exceptionCase2() {
		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(신분당선_ID, 청계산입구역_ID);

		// then : 지하철_노선_제거_실패됨 & 지하철_노선_역_이름_목록이_요청_전과_동일
		assertAll(
			() -> assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value()),
			() -> assertThat(LineAcceptanceTest.지하철_노선_역_이름_목록_조회_요청(신분당선_ID)).contains("양재역", "정자역")
		);
	}

	@DisplayName("존재하지 않는 노선에서 구간을 제거한다")
	@Test
	void removeSection_exceptionCase3() {
		// when : 지하철_구간_제거_요청
		ExtractableResponse<Response> response = 지하철_구간_제거_요청(999, 양재역_ID);

		// then : 지하철_노선_제거_실패됨 & 지하철_노선_역_이름_목록이_요청_전과_동일
		assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
	}

	public static ExtractableResponse<Response> 지하철_구간_등록_요청(
		long lineId, long upStationId, long downStationId, int distance) {
		Map<String, Object> params = new HashMap<>();
		params.put("upStationId", upStationId);
		params.put("downStationId", downStationId);
		params.put("distance", distance);

		return RestAssured
			.given().log().all()
			.body(params)
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when().post("/lines/" + lineId + "/sections")
			.then().log().all().extract();
	}

	private ExtractableResponse<Response> 지하철_구간_제거_요청(long lineId, long stationId) {
		return RestAssured
			.given().log().all()
			.when().delete("/lines/" + lineId + "/sections?stationId=" + stationId)
			.then().log().all().extract();
	}
}