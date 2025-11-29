{{/*{{- define "keycloak.fullname" -}}*/}}
{{/*{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" -}}*/}}
{{/*{{- end -}}*/}}

{{/*{{- define "keycloak.name" -}}*/}}
{{/*{{- .Chart.Name -}}*/}}
{{/*{{- end -}}*/}}

{{/*{{- define "keycloak.labels" -}}*/}}
{{/*app.kubernetes.io/name: {{ include "keycloak.name" . }}*/}}
{{/*app.kubernetes.io/instance: {{ .Release.Name }}*/}}
{{/*app.kubernetes.io/version: {{ .Chart.AppVersion }}*/}}
{{/*app.kubernetes.io/managed-by: {{ .Release.Service }}*/}}
{{/*{{- end -}}*/}}
