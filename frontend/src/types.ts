export interface EndpointInfo {
  path: string;
  method: string;
  summary: string;
  description: string;
  parameters: string[];
  requestBodySchema: string;
  responseCodes: number[];
  riskScore: number;
}

export interface TestCase {
  id: string;
  endpointPath: string;
  httpMethod: string;
  name: string;
  description: string;
  category: 'HAPPY_PATH' | 'BOUNDARY' | 'AUTH_FAILURE' | 'ERROR' | 'VALIDATION';
  riskScore: number;
  requestBody: string | null;
  expectedStatusCode: number;
  expectedResponsePattern: string;
  generatedCode?: string;
}

export interface EndpointAnalysis {
  endpoint: EndpointInfo;
  testCases: TestCase[];
  coverageStatus: 'NONE' | 'PARTIAL' | 'COVERED';
  riskScore: number;
}

export interface AnalysisResult {
  specTitle: string;
  specVersion: string;
  totalEndpoints: number;
  totalTestCases: number;
  endpoints: EndpointAnalysis[];
}

export interface CoverageGapResult {
  totalEndpoints: number;
  coveredEndpoints: number;
  partialEndpoints: number;
  uncoveredEndpoints: number;
  coveragePercent: number;
  endpoints: EndpointAnalysis[];
}
