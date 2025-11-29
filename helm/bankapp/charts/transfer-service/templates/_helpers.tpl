{{- define "transfer-service.name" -}}
transfer-service
{{- end }}

{{- define "transfer-service.fullname" -}}
{{ include "transfer-service.name" . }}
{{- end }}
