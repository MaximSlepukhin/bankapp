{{- define "exchange-generator-service.name" -}}
exchange-generator-service
{{- end -}}

{{- define "exchange-generator-service.fullname" -}}
{{ include "exchange-generator-service.name" . }}
{{- end -}}
