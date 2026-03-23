package tn.esprit.projetintegre.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projetintegre.dto.ApiResponse;
import tn.esprit.projetintegre.dto.PageResponse;
import tn.esprit.projetintegre.entities.Complaint;
import tn.esprit.projetintegre.enums.ComplaintStatus;
import tn.esprit.projetintegre.dto.response.ComplaintResponse;
import tn.esprit.projetintegre.mapper.DtoMapper;
import tn.esprit.projetintegre.services.ComplaintService;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Complaint management APIs")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final DtoMapper dtoMapper;

    @GetMapping
    @Operation(summary = "Get all complaints paginated")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintResponse>>> getAllComplaints(Pageable pageable) {
        Page<ComplaintResponse> page = complaintService.getAllComplaints(pageable).map(dtoMapper::toComplaintResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        return ResponseEntity
                .ok(ApiResponse.success(dtoMapper.toComplaintResponse(complaintService.getComplaintById(id))));
    }

    @GetMapping("/number/{complaintNumber}")
    @Operation(summary = "Get complaint by number")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintByNumber(@PathVariable String complaintNumber) {
        return ResponseEntity.ok(ApiResponse
                .success(dtoMapper.toComplaintResponse(complaintService.getComplaintByNumber(complaintNumber))));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get complaints by user ID")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintResponse>>> getComplaintsByUserId(@PathVariable Long userId,
            Pageable pageable) {
        Page<ComplaintResponse> page = complaintService.getComplaintsByUserId(userId, pageable)
                .map(dtoMapper::toComplaintResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get complaints by status")
    public ResponseEntity<ApiResponse<PageResponse<ComplaintResponse>>> getComplaintsByStatus(
            @PathVariable ComplaintStatus status, Pageable pageable) {
        Page<ComplaintResponse> page = complaintService.getComplaintsByStatus(status, pageable)
                .map(dtoMapper::toComplaintResponse);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping
    @Operation(summary = "Create a new complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(
            @RequestBody Complaint complaint,
            @RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Complaint created successfully",
                dtoMapper.toComplaintResponse(complaintService.createComplaint(complaint, userId))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(
            @PathVariable Long id,
            @RequestBody Complaint complaintDetails) {
        return ResponseEntity.ok(ApiResponse.success("Complaint updated successfully",
                dtoMapper.toComplaintResponse(complaintService.updateComplaint(id, complaintDetails))));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign complaint to staff")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @PathVariable Long id,
            @RequestParam Long assignedToId) {
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned successfully",
                dtoMapper.toComplaintResponse(complaintService.assignComplaint(id, assignedToId))));
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve a complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolveComplaint(
            @PathVariable Long id,
            @RequestParam String resolution) {
        return ResponseEntity.ok(ApiResponse.success("Complaint resolved successfully",
                dtoMapper.toComplaintResponse(complaintService.resolveComplaint(id, resolution))));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "Close a complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> closeComplaint(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint closed successfully",
                dtoMapper.toComplaintResponse(complaintService.closeComplaint(id))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a complaint")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable Long id) {
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint deleted successfully", null));
    }
}
