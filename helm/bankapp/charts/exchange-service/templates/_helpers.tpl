{{- define "exchange-service.name" -}}
exchange-service
{{- end -}}

{{- define "exchange-service.fullname" -}}
{{ .Chart.Name }}
{{- end -}}


{{- define "exchange-service.selectorLabels" -}}
app: {{ include "exchange-service.name" . }}
{{- end -}}

{{- define "exchange-service.podLabels" -}}
app: {{ include "exchange-service.name" . }}
release: {{ .Release.Name }}
{{- end -}}
