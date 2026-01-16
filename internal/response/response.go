package response

type ErrorDetail struct {
	Code    string      `json:"code,omitempty"`
	Message string      `json:"message"`
	Details interface{} `json:"details,omitempty"`
}

type Response struct {
	Data  interface{} `json:"data,omitempty"`
	Error *ErrorDetail `json:"error,omitempty"`
	Meta  interface{} `json:"meta,omitempty"`
}

func Success(data interface{}, meta interface{}) Response {
	return Response{Data: data, Meta: meta}
}

func Failure(code string, message string, details interface{}) Response {
	return Response{Error: &ErrorDetail{Code: code, Message: message, Details: details}}
}
