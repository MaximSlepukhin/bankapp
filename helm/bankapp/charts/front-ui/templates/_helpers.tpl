{{- define "front-ui.name" -}}
front-ui
{{- end -}}

{{- define "front-ui.fullname" -}}
{{ include "front-ui.name" . }}
{{- end -}}

{{/* Common labels */}}
{{- define "front-ui.labels" -}}
app.kubernetes.io/name: {{ include "front-ui.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ .Chart.AppVersion }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/* Selector labels */}}
{{- define "front-ui.selectorLabels" -}}
app.kubernetes.io/name: {{ include "front-ui.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}
