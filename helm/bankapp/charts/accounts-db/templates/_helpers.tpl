{{- define "accounts-db.name" -}}
accounts-db
{{- end }}

{{- define "accounts-db.fullname" -}}
{{ .Release.Name }}-{{ include "accounts-db.name" . }}
{{- end }}
